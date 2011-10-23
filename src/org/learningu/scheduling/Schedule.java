package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

public abstract class Schedule {
  private final Program program;
  private final Map<Room, NavigableMap<ClassPeriod, Section>> startingTimeTable;

  Schedule(Program program, Map<Room, NavigableMap<ClassPeriod, Section>> startingTimeTable) {
    this.program = checkNotNull(program);
    this.startingTimeTable = checkNotNull(startingTimeTable);
  }

  public final Iterable<Section> scheduledSections() {
    return Iterables.concat(Collections2.transform(
        startingTimeTable.values(),
        new Function<Map<ClassPeriod, Section>, Collection<Section>>() {
          @Override
          public Collection<Section> apply(Map<ClassPeriod, Section> input) {
            return input.values();
          }
        }));
  }

  public final Map<Room, Section> occurringAt(final ClassPeriod period) {
    checkNotNull(period);
    return new AbstractMap<Room, Section>() {
      @Override
      public boolean containsKey(Object room) {
        NavigableMap<ClassPeriod, Section> roomMap = startingTimeTable.get(room);
        return roomMap != null && roomMap.containsKey(period);
      }

      @Override
      public Section get(Object room) {
        NavigableMap<ClassPeriod, Section> roomMap = startingTimeTable.get(room);
        return roomMap == null ? null : roomMap.get(period);
      }

      @Override
      public Set<Entry<Room, Section>> entrySet() {
        return new AbstractSet<Entry<Room, Section>>() {
          @Override
          public Iterator<Entry<Room, Section>> iterator() {
            return new AbstractIterator<Entry<Room, Section>>() {
              final Iterator<Room> roomIterator = program.getRooms().iterator();

              @Override
              protected Entry<Room, Section> computeNext() {
                while (roomIterator.hasNext()) {
                  Room room = roomIterator.next();
                  Optional<Section> section = occurringAt(room, period);
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
        };
      }
    };
  }

  public final Set<StartAssignment> startAssignments() {
    return new StartAssignmentSet();
  }

  public final Iterable<PresentAssignment> presentAssignments() {
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
      Function<Entry<Room, NavigableMap<ClassPeriod, Section>>, Iterator<StartAssignment>> {
    INSTANCE {
      @Override
      public Iterator<StartAssignment> apply(
          Entry<Room, NavigableMap<ClassPeriod, Section>> roomEntry) {
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
          return Objects.equal(map.get(assign.getStartPeriod()), assign.getSection());
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

  public final Optional<Section> startingAt(Room room, ClassPeriod period) {
    NavigableMap<ClassPeriod, Section> scheduleForRoom = startingTimeTable.get(room);
    assert scheduleForRoom != null;
    return Optional.fromNullable(scheduleForRoom.get(period));
  }

  public final Optional<Section> occurringAt(Room room, ClassPeriod period) {
    NavigableMap<ClassPeriod, Section> scheduleForRoom = startingTimeTable.get(room);
    assert scheduleForRoom != null;
    Entry<ClassPeriod, Section> previousStart = scheduleForRoom.floorEntry(period);
    ClassPeriod startPeriod = previousStart.getKey();
    Section startSection = previousStart.getValue();
    Course startCourse = startSection.getCourse();
    if (!startPeriod.getTimeBlock().equals(period.getTimeBlock())
        || startPeriod.getIndex() + startCourse.getPeriodLength() <= period.getIndex()) {
      return Optional.absent();
    } else {
      return Optional.of(startSection);
    }
  }
}
