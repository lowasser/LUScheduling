package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.TextFormat;

/**
 * A specification for a LU Splash-type program: all the ``inputs.'' The {@code Program} object is
 * responsible for combining all the serialized program data into an actual object graph, tracking
 * associations between IDs and teachers, courses, rooms, etc.
 * 
 * @author lowasser
 */
public final class Program {
  private final ImmutableMap<Integer, Teacher> teachers;
  private final ImmutableMap<Integer, Course> courses;
  private final ImmutableMap<Integer, TimeBlock> timeBlocks;
  private final ImmutableMap<Integer, Room> rooms;
  private final ImmutableSetMultimap<Teacher, Course> teachingMap;
  private final Serial.Program serial;

  Program(Serial.Program serial) {
    this.serial = checkNotNull(serial);
    teachers = makeIdMap(Lists.transform(serial.getTeachersList(), Teacher.programWrapper(this)));
    courses = makeIdMap(Lists.transform(serial.getCoursesList(), Course.programWrapper(this)));
    rooms = makeIdMap(Lists.transform(serial.getRoomsList(), Room.programWrapper(this)));
    timeBlocks = makeIdMap(Lists.transform(serial.getTimeBlocksList(),
        TimeBlock.programWrapper(this)));

    // initialize teachingMap
    ImmutableSetMultimap.Builder<Teacher, Course> teachingMapBuilder = ImmutableSetMultimap
        .builder();
    for (Course c : courses.values()) {
      for (Teacher t : c.getTeachers()) {
        teachingMapBuilder.put(t, c);
      }
    }
    teachingMap = teachingMapBuilder.build();

    checkTeachersValid();
    checkCoursesValid();
    checkRoomsValid();
  }

  private void checkTeachersValid() {
    for (Teacher t : teachers.values()) {
      for (int block : t.serial.getAvailableBlocksList()) {
        checkArgument(timeBlocks.containsKey(block),
            "Teacher %s claims to be available at nonexistent time block with ID %s", t, block);
      }
    }
  }

  private void checkCoursesValid() {
    for (Course c : courses.values()) {
      checkArgument(c.getEstimatedClassSize() <= c.getMaxClassSize(),
          "Class %s has estimated class size %s > max class size %s", c,
          c.getEstimatedClassSize(), c.getMaxClassSize());
      for (int tId : c.serial.getTeacherIdsList()) {
        checkArgument(teachers.containsKey(tId),
            "Class %s refers to nonexistent teacher with id %s", c, tId);
      }
    }
  }

  private void checkRoomsValid() {
    for (Room r : rooms.values()) {
      for (int blockId : r.serial.getAvailableBlocksList()) {
        checkArgument(timeBlocks.containsKey(blockId),
            "Room %s claims to be available at nonexistent time block %s", r, blockId);
      }
    }
  }

  public Teacher getTeacher(int teacherId) {
    return get(teachers, teacherId);
  }

  public Course getCourse(int courseId) {
    return get(courses, courseId);
  }

  public TimeBlock getTimeBlock(int blockId) {
    return get(timeBlocks, blockId);
  }

  public Room getRoom(int roomId) {
    return get(rooms, roomId);
  }

  public Set<Course> getCoursesForTeacher(Teacher t) {
    checkArgument(getTeachers().contains(t));
    return teachingMap.get(t);
  }

  private static <T> T get(Map<Integer, T> map, int key) {
    T value = map.get(key);
    if (value == null) {
      throw new NoSuchElementException("None found for key " + key);
    }
    return value;
  }

  private static <T> void set(Map<Integer, T> map, int key, T value) {
    T oldValue = map.put(key, value);
    if (oldValue != null) {
      throw new IllegalArgumentException("Duplicate found when inserting " + value + " : "
          + oldValue);
    }
  }

  private static <T extends ProgramObject<?>> ImmutableMap<Integer, T> makeIdMap(
      Collection<T> collection) {
    Map<Integer, T> map = Maps.newLinkedHashMap();
    for (T t : collection) {
      set(map, t.getId(), t);
    }
    return ImmutableMap.copyOf(map);
  }

  private transient Set<Teacher> teacherSet;

  public Set<Teacher> getTeachers() {
    Set<Teacher> result = teacherSet;
    return (result == null) ? teacherSet = new UniqueSet<Teacher>(teachers) : result;
  }

  private transient Set<TimeBlock> timeBlockSet;

  public Set<TimeBlock> getTimeBlocks() {
    Set<TimeBlock> result = timeBlockSet;
    return (result == null) ? timeBlockSet = new UniqueSet<TimeBlock>(timeBlocks) : result;
  }

  private transient Set<Room> roomSet;

  public Set<Room> getRooms() {
    Set<Room> result = roomSet;
    return (result == null) ? roomSet = new UniqueSet<Room>(rooms) : result;
  }

  /**
   * The values of a Map<Integer, ProgramObject> map are unique. View them as a set.
   */
  private static final class UniqueSet<T extends ProgramObject<?>> extends ForwardingCollection<T>
      implements Set<T> {
    private final Map<Integer, T> idMap;

    UniqueSet(Map<Integer, T> idMap) {
      this.idMap = idMap;
    }

    @Override
    public boolean contains(@Nullable Object object) {
      if (object instanceof ProgramObject) {
        ProgramObject<?> other = (ProgramObject<?>) object;
        T lookup = idMap.get(other.getId());
        return lookup != null && lookup.equals(object);
      }
      return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
      return standardContainsAll(collection);
    }

    @Override
    public int hashCode() {
      int hashCode = 0;
      for (T t : this) {
        hashCode += t.hashCode();
      }
      return hashCode;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (obj instanceof Set) {
        Set<?> other = (Set<?>) obj;
        return size() == other.size() && containsAll(other);
      }
      return false;
    }

    @Override
    protected Collection<T> delegate() {
      return idMap.values();
    }
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

  @Override
  public String toString() {
    return TextFormat.printToString(serial);
  }
}
