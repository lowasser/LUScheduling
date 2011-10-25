package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.logic.GlobalConflict;
import org.learningu.scheduling.logic.ScheduleLogic;
import org.learningu.scheduling.logic.ScheduleValidator;
import org.learningu.scheduling.util.ModifiedState;
import org.learningu.scheduling.util.bst.BstMap;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

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

    private Schedule create(BstMap<Room, BstMap<ClassPeriod, Section>> startingTimeTable,
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

  Schedule(Factory factory, BstMap<Room, BstMap<ClassPeriod, Section>> startingTimeTable,
      BstMap<Section, StartAssignment> assignments) {
    this.startingTimeTable = checkNotNull(startingTimeTable);
    this.factory = checkNotNull(factory);
    this.assignments = checkNotNull(assignments);
  }

  public Program getProgram() {
    return factory.program;
  }

  public Set<Section> getScheduledSections() {
    return assignments.keySet();
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

  public final Set<StartAssignment> startAssignments() {
    return new StartAssignmentSet();
  }

  public Iterable<PresentAssignment> presentAssignments() {
    return Iterables.concat(Iterables.transform(
        startAssignments(),
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
      return Iterators.concat(Iterators.transform(
          startingTimeTable.entrySet().iterator(),
          TransformFunction.INSTANCE));
    }

    @Override
    public boolean contains(Object o) {
      if (o instanceof StartAssignment) {
        StartAssignment assign = (StartAssignment) o;
        Map<ClassPeriod, Section> map = startingTimeTable.get(assign.getRoom());
        if (map != null) {
          return Objects.equal(map.get(assign.getPeriod()), assign.getCourse());
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
      if (relativeIndex < prevStart.getCourse().getPeriodLength()) {
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
                  roomMap.insert(assign.getPeriod(), assign.getCourse())),
              assignments.insert(assign.getCourse(), assign)));
    } else {
      return ModifiedState.of(validator, this);
    }
  }

  public ModifiedState<Optional<StartAssignment>, Schedule> removeStartingAt(ClassPeriod period,
      Room room) {
    Optional<StartAssignment> startingAt = startingAt(period, room);
    Schedule revised = this;
    if (startingAt.isPresent()) {
      BstMap<ClassPeriod, Section> roomMap = startingTimeTable.get(room);
      revised = factory.create(
          startingTimeTable.insert(room, roomMap.delete(period)),
          assignments.delete(startingAt.get().getCourse()));
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

  @Override
  public int hashCode() {
    return Objects.hashCode(startAssignments(), getProgram());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Schedule) {
      Schedule other = (Schedule) obj;
      return Objects.equal(startAssignments(), other.startAssignments())
          && getProgram().equals(other.getProgram());
    }
    return false;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("startAssignments", startAssignments()).toString();
  }
}
