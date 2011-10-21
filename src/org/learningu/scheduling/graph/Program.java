package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.learningu.scheduling.graph.Serial.SerialProgram;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.Weigher;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
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
  final ProgramObjectSet<Teacher> teachers;
  final ProgramObjectSet<Course> courses;
  final ProgramObjectSet<TimeBlock> timeBlocks;
  final ProgramObjectSet<Room> rooms;
  private final ImmutableSetMultimap<Teacher, Course> teachingMap;
  private final SerialProgram serial;
  private final Cache<Course, Set<TimeBlock>> courseCompatibleBlocks;
  private final Cache<Room, Set<TimeBlock>> roomAvailableBlocks;
  private final Cache<Teacher, Set<TimeBlock>> teacherAvailableBlocks;
  private final Cache<Course, Set<Teacher>> teachersForCourse;

  @VisibleForTesting
  Program(SerialProgram serial) {
    this(serial, ProgramCacheFlags.DEFAULTS);
  }

  @Inject
  Program(SerialProgram serial, ProgramCacheFlags flags) {
    checkNotNull(flags);
    this.serial = checkNotNull(serial);
    teachers = ProgramObjectSet.create(Lists.transform(
        serial.getTeachersList(),
        Teacher.programWrapper(this)));
    courses = ProgramObjectSet.create(Lists.transform(
        serial.getCoursesList(),
        Course.programWrapper(this)));
    rooms = ProgramObjectSet.create(Lists.transform(
        serial.getRoomsList(),
        Room.programWrapper(this)));
    timeBlocks = ProgramObjectSet.create(Lists.transform(
        serial.getTimeBlocksList(),
        TimeBlock.programWrapper(this)));

    // initialize teachingMap
    ImmutableSetMultimap.Builder<Teacher, Course> teachingMapBuilder = ImmutableSetMultimap.builder();
    for (Course c : courses) {
      for (Teacher t : c.getTeachers()) {
        teachingMapBuilder.put(t, c);
      }
    }
    teachingMap = teachingMapBuilder.build();

    checkTeachersValid();
    checkCoursesValid();
    checkRoomsValid();

    this.teacherAvailableBlocks = CacheBuilder.newBuilder()
        .initialCapacity(teachers.size())
        .concurrencyLevel(flags.cacheConcurrencyLevel)
        .weigher(COLLECTION_WEIGHER)
        .maximumWeight(flags.teacherAvailableCacheSize)
        .build(new CacheLoader<Teacher, Set<TimeBlock>>() {
          @Override
          public Set<TimeBlock> load(Teacher key) throws Exception {
            return key.getCompatibleTimeBlocks();
          }
        });
    this.roomAvailableBlocks = CacheBuilder.newBuilder()
        .initialCapacity(rooms.size())
        .concurrencyLevel(flags.cacheConcurrencyLevel)
        .weigher(COLLECTION_WEIGHER)
        .maximumWeight(flags.roomAvailableCacheSize)
        .build(new CacheLoader<Room, Set<TimeBlock>>() {
          @Override
          public Set<TimeBlock> load(Room key) throws Exception {
            return key.getCompatibleTimeBlocks();
          }
        });
    this.teachersForCourse = CacheBuilder.newBuilder()
        .initialCapacity(courses.size())
        .weigher(COLLECTION_WEIGHER)
        .concurrencyLevel(flags.cacheConcurrencyLevel)
        .maximumWeight(flags.courseTeachersCacheSize)
        .build(new CacheLoader<Course, Set<Teacher>>() {
          @Override
          public Set<Teacher> load(Course key) throws Exception {
            return key.getTeachers();
          }
        });
    this.courseCompatibleBlocks = CacheBuilder.newBuilder()
        .initialCapacity(courses.size())
        .weigher(COLLECTION_WEIGHER)
        .concurrencyLevel(flags.cacheConcurrencyLevel)
        .maximumWeight(flags.courseCompatibleCacheSize)
        .build(new CacheLoader<Course, Set<TimeBlock>>() {
          @Override
          public Set<TimeBlock> load(Course key) throws Exception {
            Set<TimeBlock> blocks = Sets.newLinkedHashSet(getTimeBlocks());
            for (Teacher t : teachersForCourse(key)) {
              blocks.retainAll(compatibleTimeBlocks(t));
            }
            return ImmutableSet.copyOf(blocks);
          }
        });
  }

  private static final Weigher<Object, Collection<?>> COLLECTION_WEIGHER = new Weigher<Object, Collection<?>>() {
    @Override
    public int weigh(Object key, Collection<?> value) {
      return value.size();
    }
  };

  public Set<Teacher> teachersForCourse(Course c) {
    return teachersForCourse.getUnchecked(c);
  }

  public Set<TimeBlock> compatibleTimeBlocks(Course c) {
    return courseCompatibleBlocks.getUnchecked(c);
  }

  public Set<TimeBlock> compatibleTimeBlocks(Teacher t) {
    return teacherAvailableBlocks.getUnchecked(t);
  }

  public Set<TimeBlock> compatibleTimeBlocks(Room r) {
    return roomAvailableBlocks.getUnchecked(r);
  }

  private void checkTeachersValid() {
    for (Teacher t : teachers) {
      t.getCompatibleTimeBlocks();
    }
  }

  private void checkCoursesValid() {
    for (Course c : courses) {
      checkArgument(
          c.getEstimatedClassSize() <= c.getMaxClassSize(),
          "Class %s has estimated class size %s > max class size %s",
          c,
          c.getEstimatedClassSize(),
          c.getMaxClassSize());
      c.getTeachers();
    }
  }

  private void checkRoomsValid() {
    for (Room r : rooms) {
      r.getCompatibleTimeBlocks();
    }
  }

  public Teacher getTeacher(int teacherId) {
    return teachers.getForId(teacherId);
  }

  public Course getCourse(int courseId) {
    return courses.getForId(courseId);
  }

  public TimeBlock getTimeBlock(int blockId) {
    return timeBlocks.getForId(blockId);
  }

  public Room getRoom(int roomId) {
    return rooms.getForId(roomId);
  }

  public Set<Course> getCoursesForTeacher(Teacher t) {
    checkArgument(teachers.contains(t));
    return teachingMap.get(t);
  }

  public Set<Teacher> getTeachers() {
    return teachers;
  }

  public Set<Course> getCourses() {
    return courses;
  }

  public Set<TimeBlock> getTimeBlocks() {
    return timeBlocks;
  }

  public Set<Room> getRooms() {
    return rooms;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(teachers, rooms, courses, timeBlocks);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof Program) {
      Program other = (Program) obj;
      return teachers.equals(other.teachers) && rooms.equals(other.rooms)
          && courses.equals(other.courses) && timeBlocks.equals(other.timeBlocks);
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
