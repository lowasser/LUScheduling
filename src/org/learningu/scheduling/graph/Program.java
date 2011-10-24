package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.learningu.scheduling.graph.Serial.SerialProgram;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.Weigher;
import com.google.common.collect.Collections2;
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
  final ProgramObjectSet<Teacher> teachers;

  final ProgramObjectSet<Course> courses;

  final ProgramObjectSet<TimeBlock> timeBlocks;

  final ProgramObjectSet<Room> rooms;

  final ProgramObjectSet<ClassPeriod> periods;

  final ProgramObjectSet<RoomProperty> roomProperties;

  private final ImmutableSetMultimap<Teacher, Course> teachingMap;

  private final SerialProgram serial;

  private final Cache<Course, Set<ClassPeriod>> courseCompatiblePeriods;

  private final Cache<Room, Set<ClassPeriod>> roomAvailablePeriods;

  private final Cache<Teacher, Set<ClassPeriod>> teacherAvailablePeriods;

  private final Cache<Course, Set<Teacher>> teachersForCourse;

  private final Cache<Course, Set<RoomProperty>> requiredForCourse;

  private final Cache<Room, Set<RoomProperty>> propertiesOfRoom;

  private final ImmutableSet<Section> sections;

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
    periods = ProgramObjectSet.create(Iterables.concat(Collections2.transform(
        getTimeBlocks(),
        new Function<TimeBlock, List<ClassPeriod>>() {
          @Override
          public List<ClassPeriod> apply(TimeBlock input) {
            return input.getPeriods();
          }
        })));
    roomProperties = ProgramObjectSet.create(Lists.transform(
        serial.getRoomPropertiesList(),
        RoomProperty.programWrapper(this)));

    // initialize teachingMap
    ImmutableSetMultimap.Builder<Teacher, Course> teachingMapBuilder = ImmutableSetMultimap
        .builder();
    for (Course c : courses) {
      for (Teacher t : c.getTeachers()) {
        teachingMapBuilder.put(t, c);
      }
    }
    teachingMap = teachingMapBuilder.build();

    checkTeachersValid();
    checkCoursesValid();
    checkRoomsValid();

    this.teacherAvailablePeriods = CacheBuilder
        .newBuilder()
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
    this.roomAvailablePeriods = CacheBuilder
        .newBuilder()
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
    this.teachersForCourse = CacheBuilder
        .newBuilder()
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
    this.courseCompatiblePeriods = CacheBuilder
        .newBuilder()
        .initialCapacity(courses.size())
        .weigher(COLLECTION_WEIGHER)
        .concurrencyLevel(flags.cacheConcurrencyLevel)
        .maximumWeight(flags.courseCompatibleCacheSize)
        .build(new CacheLoader<Course, Set<ClassPeriod>>() {
          @Override
          public Set<ClassPeriod> load(Course key) {
            Set<ClassPeriod> blocks = Sets.newLinkedHashSet(getPeriods());
            for (Teacher t : teachersForCourse(key)) {
              blocks.retainAll(compatiblePeriods(t));
            }
            return ImmutableSet.copyOf(blocks);
          }
        });
    this.requiredForCourse = CacheBuilder
        .newBuilder()
        .initialCapacity(courses.size())
        .weigher(COLLECTION_WEIGHER)
        .maximumWeight(flags.reqPropsCacheSize)
        .build(new CacheLoader<Course, Set<RoomProperty>>() {
          @Override
          public Set<RoomProperty> load(Course key) {
            return key.getRequiredProperties();
          }
        });
    this.propertiesOfRoom = CacheBuilder
        .newBuilder()
        .initialCapacity(rooms.size())
        .weigher(COLLECTION_WEIGHER)
        .maximumWeight(flags.reqPropsCacheSize)
        .build(new CacheLoader<Room, Set<RoomProperty>>() {
          @Override
          public Set<RoomProperty> load(Room key) throws Exception {
            return key.getRoomProperties();
          }
        });

    sections = ImmutableSet.copyOf(Iterables.concat(Collections2.transform(
        getCourses(),
        new Function<Course, List<Section>>() {
          @Override
          public List<Section> apply(Course input) {
            return input.getSections();
          }
        })));
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

  public Set<ClassPeriod> compatiblePeriods(Course c) {
    return courseCompatiblePeriods.getUnchecked(c);
  }

  public Set<ClassPeriod> compatiblePeriods(Teacher t) {
    return teacherAvailablePeriods.getUnchecked(t);
  }

  public Set<ClassPeriod> compatiblePeriods(Room r) {
    return roomAvailablePeriods.getUnchecked(r);
  }

  public Set<RoomProperty> roomRequirements(Course c) {
    return requiredForCourse.getUnchecked(c);
  }

  public Set<RoomProperty> roomProperties(Room r) {
    return propertiesOfRoom.getUnchecked(r);
  }

  private void checkTeachersValid() {
    for (Teacher t : teachers) {
      t.getCompatiblePeriods();
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
      r.getCompatiblePeriods();
    }
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

  public Set<Section> getSections() {
    return sections;
  }

  public Set<ClassPeriod> getPeriods() {
    return periods;
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
