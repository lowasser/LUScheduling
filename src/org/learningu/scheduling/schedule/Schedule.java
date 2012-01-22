package org.learningu.scheduling.schedule;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.logic.GlobalConflict;
import org.learningu.scheduling.logic.ScheduleLogic;
import org.learningu.scheduling.logic.ScheduleValidator;
import org.learningu.scheduling.util.ModifiedState;
import org.learningu.scheduling.util.bst.BstMap;

/**
 * An immutable representation of a schedule, with an associated LU {@link Program} and
 * {@link ScheduleLogic} instance for schedule validity checking. Although this object is
 * immutable, it supports efficient updates and a variety of queries, including map views of parts
 * of the schedule in various rooms or at certain times. Attempts to add schedule assignments that
 * result in conflict, according to this schedule's {@code ScheduleLogic}, will fail.
 * 
 * @author lowasser
 */
public final class Schedule {
  public static final class Factory {
    private final Program program;

    private final ScheduleLogic logic;

    private final Provider<ScheduleValidator> validatorProvider;

    @Inject
    Factory(Program program, ScheduleLogic logic, Provider<ScheduleValidator> validatorProvider) {
      this.program = program;
      this.logic = logic;
      this.validatorProvider = validatorProvider;
    }

    private Schedule create(
        BstMap<Room, BstMap<ClassPeriod, Section>> startingTimeTable,
        BstMap<Section, StartAssignment> assignments) {
      return new Schedule(this, startingTimeTable, assignments);
    }

    public Schedule create() {
      BstMap<Room, BstMap<ClassPeriod, Section>> roomMap = BstMap.create();
      for (Room r : program.getRooms()) {
        roomMap = roomMap.insert(r, BstMap.<ClassPeriod, Section> create());
      }
      return new Schedule(this, roomMap, BstMap.<Section, StartAssignment> create());
    }
  }

  private final Factory factory;

  private final BstMap<Room, BstMap<ClassPeriod, Section>> startingTimeTable;

  private final BstMap<Section, StartAssignment> assignments;

  Schedule(
      Factory factory,
      BstMap<Room, BstMap<ClassPeriod, Section>> startingTimeTable,
      BstMap<Section, StartAssignment> assignments) {
    this.startingTimeTable = checkNotNull(startingTimeTable);
    this.factory = checkNotNull(factory);
    this.assignments = checkNotNull(assignments);
  }

  public boolean isCompletelyValid() {
    Schedule base = factory.create();
    for (StartAssignment assign : getStartAssignments()) {
      ModifiedState<ScheduleValidator, Schedule> modifiedState = base.assignStart(assign);
      assert modifiedState.getResult().isValid();
    }
    return true;
  }

  public Program getProgram() {
    return factory.program;
  }

  public Set<Section> getScheduledSections() {
    return assignments.keySet();
  }

  public Map<Section, StartAssignment> getAssignmentsBySection() {
    return assignments;
  }

  public final Map<ClassPeriod, StartAssignment> startingIn(final Room room) {
    return Maps.transformEntries(
        startingTimeTable.get(room),
        new EntryTransformer<ClassPeriod, Section, StartAssignment>() {

          @Override
          public StartAssignment transformEntry(ClassPeriod period, Section section) {
            return StartAssignment.create(period, room, section);
          }
        });
  }

  public final List<StartAssignment> courseSectionAssignments(Course course) {
    Set<Section> sections = getProgram().getSectionsOfCourse(course);
    List<StartAssignment> assigns = Lists.newArrayListWithCapacity(sections.size());
    for (Section s : sections) {
      StartAssignment assign = assignments.get(s);
      if (assign != null) {
        assigns.add(assign);
      }
    }
    return Collections.unmodifiableList(assigns);
  }

  public final Map<Room, PresentAssignment> occurringAt(final ClassPeriod period) {
    checkNotNull(period);
    return new OccurringAtMap(period);
  }

  private final class OccurringAtMap extends AbstractMap<Room, PresentAssignment> {
    private final class OccurringAtEntrySet extends AbstractSet<Entry<Room, PresentAssignment>> {
      @Override
      public boolean contains(Object o) {
        if (o instanceof Entry) {
          Entry<?, ?> entry = (Entry<?, ?>) o;
          return Objects.equal(get(entry.getKey()), entry.getValue());
        }
        return false;
      }

      @Override
      public Iterator<Entry<Room, PresentAssignment>> iterator() {
        return new AbstractIterator<Entry<Room, PresentAssignment>>() {
          final Iterator<Room> roomIterator = getProgram().getRooms().iterator();

          @Override
          protected Entry<Room, PresentAssignment> computeNext() {
            while (roomIterator.hasNext()) {
              Room room = roomIterator.next();
              Optional<PresentAssignment> section = occurringAt(period, room);
              if (section.isPresent()) {
                return Maps.immutableEntry(room, section.get());
              }
            }
            return endOfData();
          }
        };
      }

      @Override
      public int size() {
        return Iterators.size(iterator());
      }
    }

    private final ClassPeriod period;

    private OccurringAtMap(ClassPeriod period) {
      this.period = period;
    }

    @Override
    public boolean containsKey(Object room) {
      BstMap<ClassPeriod, Section> roomMap = startingTimeTable.get(room);
      return roomMap != null && roomMap.containsKey(period);
    }

    @Override
    public PresentAssignment get(Object room) {
      try {
        return occurringAt(period, (Room) room).orNull();
      } catch (ClassCastException e) {
        return null;
      }
    }

    @Override
    public Set<Entry<Room, PresentAssignment>> entrySet() {
      return new OccurringAtEntrySet();
    }
  }

  public final Set<StartAssignment> getStartAssignments() {
    return new StartAssignmentSet();
  }

  public Iterable<PresentAssignment> getPresentAssignments() {
    return Iterables.concat(Iterables.transform(
        getStartAssignments(),
        new Function<StartAssignment, List<PresentAssignment>>() {

          @Override
          public List<PresentAssignment> apply(StartAssignment input) {
            return input.getPresentAssignments();
          }
        }));
  }

  static enum TransformFunction implements
      Function<Entry<Room, ? extends Map<ClassPeriod, Section>>, Iterator<StartAssignment>> {
    INSTANCE {
      @Override
      public Iterator<StartAssignment> apply(
          Entry<Room, ? extends Map<ClassPeriod, Section>> roomEntry) {
        final Room room = roomEntry.getKey();
        return Iterators.transform(
            roomEntry.getValue().entrySet().iterator(),
            new Function<Entry<ClassPeriod, Section>, StartAssignment>() {
              @Override
              public StartAssignment apply(Entry<ClassPeriod, Section> pdEntry) {
                return StartAssignment.create(pdEntry.getKey(), room, pdEntry.getValue());
              }
            });
      }
    };
  }

  private final class StartAssignmentSet extends AbstractSet<StartAssignment> {

    @Override
    public Iterator<StartAssignment> iterator() {
      return assignments.values().iterator();
    }

    @Override
    public boolean contains(Object o) {
      if (o instanceof StartAssignment) {
        StartAssignment assign = (StartAssignment) o;
        Map<ClassPeriod, Section> map = startingTimeTable.get(assign.getRoom());
        if (map != null) {
          return Objects.equal(map.get(assign.getPeriod()), assign.getSection());
        }
      }
      return false;
    }

    @Override
    public int size() {
      int result = 0;
      for (Map<ClassPeriod, Section> map : startingTimeTable.values()) {
        result += map.size();
      }
      return result;
    }
  }

  public Optional<StartAssignment> startingAt(ClassPeriod period, Room room) {
    BstMap<ClassPeriod, Section> scheduleForRoom = startingTimeTable.get(room);
    assert scheduleForRoom != null;
    Section section = scheduleForRoom.get(period);
    if (section != null) {
      return Optional.of(StartAssignment.create(period, room, section));
    } else {
      return Optional.absent();
    }
  }

  public Optional<StartAssignment> startingBefore(Room room, ClassPeriod period) {
    BstMap<ClassPeriod, Section> scheduleForRoom = startingTimeTable.get(room);
    assert scheduleForRoom != null;
    Entry<ClassPeriod, Section> floorEntry = scheduleForRoom.floorEntry(period);
    if (floorEntry != null && floorEntry.getKey().getTimeBlock().equals(period.getTimeBlock())) {
      return Optional.of(StartAssignment.create(floorEntry.getKey(), room, floorEntry.getValue()));
    } else {
      return Optional.absent();
    }
  }

  public Optional<PresentAssignment> occurringAt(ClassPeriod period, Room room) {
    Optional<StartAssignment> startingBefore = startingBefore(room, period);
    if (startingBefore.isPresent()) {
      StartAssignment prevStart = startingBefore.get();
      int relativeIndex = period.getIndex() - prevStart.getPeriod().getIndex();
      if (relativeIndex >= 0 && relativeIndex < prevStart.getSection().getPeriodLength()) {
        return Optional.of(prevStart.getPresentAssignment(relativeIndex));
      }
    }
    return Optional.absent();
  }

  public ModifiedState<ScheduleValidator, Schedule> assignStart(StartAssignment assign) {
    ScheduleValidator validator = factory.validatorProvider.get();
    factory.logic.validate(validator, this, assign);
    for (PresentAssignment pAssign : assign.getPresentAssignments()) {
      factory.logic.validate(validator, this, pAssign);
    }
    if (validator.isValid()) {
      BstMap<ClassPeriod, Section> roomMap = startingTimeTable.get(assign.getRoom());
      return ModifiedState.of(
          validator,
          factory.create(
              startingTimeTable.insert(
                  assign.getRoom(),
                  roomMap.insert(assign.getPeriod(), assign.getSection())),
              assignments.insert(assign.getSection(), assign)));
    } else {
      return ModifiedState.of(validator, this);
    }
  }

  public ModifiedState<Optional<StartAssignment>, Schedule> removeStartingAt(
      ClassPeriod period,
      Room room) {
    Optional<StartAssignment> startingAt = startingAt(period, room);
    Schedule revised = this;
    if (startingAt.isPresent()) {
      BstMap<ClassPeriod, Section> roomMap = startingTimeTable.get(room);
      revised = factory.create(
          startingTimeTable.insert(room, roomMap.delete(period)),
          assignments.delete(startingAt.get().getSection()));
    }
    return ModifiedState.of(startingAt, revised);
  }

  public ModifiedState<ScheduleValidator, Schedule> forceAssignStart(StartAssignment assign) {
    ScheduleValidator validator = factory.validatorProvider.get();
    factory.logic.validate(validator, this, assign);
    for (PresentAssignment pAssign : assign.getPresentAssignments()) {
      factory.logic.validate(validator, this, pAssign);
    }
    if (!validator.isLocallyValid()) {
      return ModifiedState.of(validator, this);
    }
    Schedule revised = this;
    for (GlobalConflict<PresentAssignment> conflict : validator.getGlobalPresentConflicts()) {
      for (PresentAssignment conflicting : conflict.getConflictingAssignments()) {
        StartAssignment toRemove = conflicting.getStartAssignment();
        revised = removeStartingAt(toRemove.getPeriod(), toRemove.getRoom()).getNewState();
      }
    }
    for (GlobalConflict<StartAssignment> conflict : validator.getGlobalStartConflicts()) {
      for (StartAssignment conflicting : conflict.getConflictingAssignments()) {
        revised = removeStartingAt(conflicting.getPeriod(), conflicting.getRoom()).getNewState();
      }
    }
    return revised.assignStart(assign);
  }

  private transient int hashCode = -1;

  @Override
  public int hashCode() {
    int result = hashCode;
    if (result == -1) {
      return hashCode = Objects.hashCode(getStartAssignments(), getProgram());
    } else {
      return result;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof Schedule) {
      Schedule other = (Schedule) obj;
      if (hashCode != -1 && other.hashCode != -1 && hashCode != other.hashCode) {
        return false;
      }
      return Objects.equal(getStartAssignments(), other.getStartAssignments())
          && getProgram().equals(other.getProgram());
    }
    return false;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("startAssignments", getStartAssignments()).toString();
  }
}
