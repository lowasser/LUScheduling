package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.learningu.scheduling.graph.SerialGraph.SerialProgram;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.Weigher;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.TextFormat;

/**
 * A specification for a LU Splash-type program: all the ``inputs.'' The {@code Program} object is
 * responsible for combining all the serialized program data into an actual object graph, tracking
 * associations between IDs and teachers, courses, rooms, etc.
 * 
 * @author lowasser
 */
@Singleton
public final class Program {
  final ImmutableBiMap<Integer, Teacher> teachers;

  final ImmutableBiMap<Integer, Section> sections;

  final ImmutableBiMap<Integer, TimeBlock> timeBlocks;

  final ImmutableBiMap<Integer, Room> rooms;

  final ImmutableBiMap<Integer, ClassPeriod> periods;

  final ImmutableBiMap<Integer, RoomProperty> roomProperties;

  private final ImmutableSetMultimap<Teacher, Section> teachingMap;

  private final SerialProgram serial;

  private final Cache<Section, Set<ClassPeriod>> courseCompatiblePeriods;

  private final Cache<Room, Set<ClassPeriod>> roomAvailablePeriods;

  private final Cache<Teacher, Set<ClassPeriod>> teacherAvailablePeriods;

  private final Cache<Section, Set<Teacher>> teachersForCourse;

  private final Cache<Section, Set<RoomProperty>> requiredForCourse;

  private final Cache<Room, Set<RoomProperty>> propertiesOfRoom;

  @VisibleForTesting
  Program(SerialProgram serial) {
    this(serial, ProgramCacheFlags.DEFAULTS);
  }
  
  public SerialProgram getSerial() {
    return serial;
  }

  @Inject
  Program(SerialProgram serial, ProgramCacheFlags flags) {
    checkNotNull(flags);
    this.serial = checkNotNull(serial);
    teachers =
        programObjectSet(Lists.transform(serial.getTeacherList(), Teacher.programWrapper(this)));
    sections =
        programObjectSet(Lists.transform(serial.getSectionList(), Section.programWrapper(this)));
    rooms = programObjectSet(Lists.transform(serial.getRoomList(), Room.programWrapper(this)));
    timeBlocks =
        programObjectSet(Lists.transform(serial.getTimeBlockList(), TimeBlock.programWrapper(this)));
    periods =
        programObjectSet(Iterables.concat(Collections2.transform(
            getTimeBlocks(),
            new Function<TimeBlock, List<ClassPeriod>>() {
              @Override
              public List<ClassPeriod> apply(TimeBlock input) {
                return input.getPeriods();
              }
            })));
    roomProperties =
        programObjectSet(Lists.transform(
            serial.getRoomPropertyList(),
            RoomProperty.programWrapper(this)));

    // initialize teachingMap
    ImmutableSetMultimap.Builder<Teacher, Section> teachingMapBuilder =
        ImmutableSetMultimap.builder();
    for (Section c : getSections()) {
      for (Teacher t : c.getTeachers()) {
        teachingMapBuilder.put(t, c);
      }
    }
    teachingMap = teachingMapBuilder.build();

    checkTeachersValid();
    checkCoursesValid();
    checkRoomsValid();

    this.teacherAvailablePeriods =
        CacheBuilder.newBuilder()
            .initialCapacity(teachers.size())
            .concurrencyLevel(flags.cacheConcurrencyLevel)
            .weigher(COLLECTION_WEIGHER)
            .maximumWeight(flags.teacherAvailableCacheSize)
            .build(new CacheLoader<Teacher, Set<ClassPeriod>>() {
              @Override
              public Set<ClassPeriod> load(Teacher key) {
                return key.getCompatiblePeriods();
              }
            });
    this.roomAvailablePeriods =
        CacheBuilder.newBuilder()
            .initialCapacity(rooms.size())
            .concurrencyLevel(flags.cacheConcurrencyLevel)
            .weigher(COLLECTION_WEIGHER)
            .maximumWeight(flags.roomAvailableCacheSize)
            .build(new CacheLoader<Room, Set<ClassPeriod>>() {
              @Override
              public Set<ClassPeriod> load(Room key) {
                return key.getCompatiblePeriods();
              }
            });
    this.teachersForCourse =
        CacheBuilder.newBuilder()
            .initialCapacity(sections.size())
            .weigher(COLLECTION_WEIGHER)
            .concurrencyLevel(flags.cacheConcurrencyLevel)
            .maximumWeight(flags.courseTeachersCacheSize)
            .build(new CacheLoader<Section, Set<Teacher>>() {
              @Override
              public Set<Teacher> load(Section key) throws Exception {
                return key.getTeachers();
              }
            });
    this.courseCompatiblePeriods =
        CacheBuilder.newBuilder()
            .initialCapacity(sections.size())
            .weigher(COLLECTION_WEIGHER)
            .concurrencyLevel(flags.cacheConcurrencyLevel)
            .maximumWeight(flags.courseCompatibleCacheSize)
            .build(new CacheLoader<Section, Set<ClassPeriod>>() {
              @Override
              public Set<ClassPeriod> load(Section key) {
                Set<ClassPeriod> blocks = Sets.newLinkedHashSet(getPeriods());
                for (Teacher t : teachersForCourse(key)) {
                  blocks.retainAll(compatiblePeriods(t));
                }
                return ImmutableSet.copyOf(blocks);
              }
            });
    this.requiredForCourse =
        CacheBuilder.newBuilder()
            .initialCapacity(sections.size())
            .weigher(COLLECTION_WEIGHER)
            .maximumWeight(flags.reqPropsCacheSize)
            .build(new CacheLoader<Section, Set<RoomProperty>>() {
              @Override
              public Set<RoomProperty> load(Section key) {
                return key.getRequiredProperties();
              }
            });
    this.propertiesOfRoom =
        CacheBuilder.newBuilder()
            .initialCapacity(rooms.size())
            .weigher(COLLECTION_WEIGHER)
            .maximumWeight(flags.reqPropsCacheSize)
            .build(new CacheLoader<Room, Set<RoomProperty>>() {
              @Override
              public Set<RoomProperty> load(Room key) throws Exception {
                return key.getRoomProperties();
              }
            });
  }

  private static <T extends ProgramObject<?>> ImmutableBiMap<Integer, T> programObjectSet(
      Iterable<T> collection) {
    Builder<Integer, T> builder = ImmutableBiMap.builder();
    for (T t : collection) {
      builder.put(t.getId(), t);
    }
    return builder.build();
  }

  private static final Weigher<Object, Collection<?>> COLLECTION_WEIGHER =
      new Weigher<Object, Collection<?>>() {
        @Override
        public int weigh(Object key, Collection<?> value) {
          return value.size();
        }
      };

  public Set<Teacher> teachersForCourse(Section c) {
    return teachersForCourse.getUnchecked(c);
  }

  public Set<ClassPeriod> compatiblePeriods(Section c) {
    return courseCompatiblePeriods.getUnchecked(c);
  }

  public Set<ClassPeriod> compatiblePeriods(Teacher t) {
    return teacherAvailablePeriods.getUnchecked(t);
  }

  public Set<ClassPeriod> compatiblePeriods(Room r) {
    return roomAvailablePeriods.getUnchecked(r);
  }

  public Set<RoomProperty> roomRequirements(Section c) {
    return requiredForCourse.getUnchecked(c);
  }

  public Set<RoomProperty> roomProperties(Room r) {
    return propertiesOfRoom.getUnchecked(r);
  }

  public ClassPeriod getPeriod(int id) {
    ClassPeriod classPeriod = periods.get(id);
    checkArgument(classPeriod != null, "No period with id %s", id);
    return classPeriod;
  }

  public Room getRoom(int id) {
    Room room = rooms.get(id);
    checkArgument(room != null, "No such room with id %s", id);
    return room;
  }

  public Section getSection(int id) {
    Section section = sections.get(id);
    checkArgument(section != null, "No such section with id %s", id);
    return section;
  }

  private void checkTeachersValid() {
    for (Teacher t : getTeachers()) {
      t.getCompatiblePeriods();
    }
  }

  private void checkCoursesValid() {
    for (Section c : getSections()) {
      checkArgument(
          c.getEstimatedClassSize() <= c.getMaxClassSize(),
          "Class %s has estimated class size %s > max class size %s",
          c,
          c.getEstimatedClassSize(),
          c.getMaxClassSize());
      c.getTeachers();
      c.getRequiredProperties();
    }
  }

  private void checkRoomsValid() {
    for (Room r : getRooms()) {
      r.getCompatiblePeriods();
    }
  }

  public Set<Section> getCoursesForTeacher(Teacher t) {
    checkArgument(teachers.containsValue(t));
    return teachingMap.get(t);
  }

  public Set<Teacher> getTeachers() {
    return teachers.values();
  }

  public Set<Section> getSections() {
    return sections.values();
  }

  public Set<TimeBlock> getTimeBlocks() {
    return timeBlocks.values();
  }

  public Set<Room> getRooms() {
    return rooms.values();
  }

  public Set<ClassPeriod> getPeriods() {
    return periods.values();
  }

  public Set<RoomProperty> getRoomProperties() {
    return roomProperties.values();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(teachers, rooms, sections, timeBlocks);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof Program) {
      Program other = (Program) obj;
      return teachers.equals(other.teachers) && rooms.equals(other.rooms)
          && sections.equals(other.sections) && timeBlocks.equals(other.timeBlocks);
    }
    return false;
  }

  public String getName() {
    return serial.getName();
  }

  @Override
  public String toString() {
    return TextFormat.printToString(serial);
  }
}
