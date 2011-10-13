package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.TextFormat;

public final class Program {
  private final ImmutableMap<Integer, Teacher> teachers;
  private final ImmutableMap<Integer, Course> courses;
  private final ImmutableMap<Integer, RoomProperty> roomProperties;
  private final ImmutableMap<Integer, TimeBlock> timeBlocks;
  private final ImmutableMap<Integer, Room> rooms;
  private final ImmutableSetMultimap<Teacher, Course> teachingMap;
  private final ImmutableMap<TimeBlock, Integer> timeBlockIndex;
  private final Serial.Program serial;

  Program(Serial.Program serial) {
    this.serial = checkNotNull(serial);
    teachers = makeIdMap(Lists.transform(serial.getTeachersList(), Teacher.programWrapper(this)));
    courses = makeIdMap(Lists.transform(serial.getCoursesList(), Course.programWrapper(this)));
    roomProperties = makeIdMap(Lists.transform(serial.getRoomPropertiesList(),
        RoomProperty.programWrapper(this)));
    rooms = makeIdMap(Lists.transform(serial.getRoomsList(), Room.programWrapper(this)));

    // initialize timeBlocks
    List<Serial.TimeBlock> orderedBlocks = orderedTimeBlocks(Maps.uniqueIndex(
        serial.getTimeBlocksList(), new Function<Serial.TimeBlock, Integer>() {

          @Override
          public Integer apply(org.learningu.scheduling.Serial.TimeBlock input) {
            return input.getBlockId();
          }
        }));
    List<TimeBlock> blocks = Lists.transform(orderedBlocks, TimeBlock.programWrapper(this));
    timeBlocks = makeIdMap(blocks);
    timeBlockIndex = reverseIndex(blocks);

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
      checkArgument(c.getNumberOfSections() <= timeBlocks.size(),
          "Class %s wants to schedule more (%s) sections than there are time blocks (%s)", c,
          c.getNumberOfSections(), timeBlocks.size());
      for (int resId : c.serial.getRoomRequiredPropertiesList()) {
        checkArgument(roomProperties.containsKey(resId),
            "Class %s refers to nonexistent room property with id %s", c, resId);
      }
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
      for (int resId : r.serial.getPropertiesList()) {
        checkArgument(roomProperties.containsKey(resId),
            "Room %s claims to have nonexistent room property %s", r, resId);
      }
    }
  }

  private static <E> ImmutableMap<E, Integer> reverseIndex(List<E> list) {
    ImmutableMap.Builder<E, Integer> builder = ImmutableMap.builder();
    for (int i = 0; i < list.size(); i++) {
      builder.put(list.get(i), i);
    }
    return builder.build();
  }

  private static Serial.TimeBlock firstTimeBlock(Map<Integer, Serial.TimeBlock> timeBlocks) {
    Serial.TimeBlock any = timeBlocks.values().iterator().next();
    while (any.hasPrevTime()) {
      any = timeBlocks.get(any.getPrevTime());
    }
    return any;
  }

  private static List<Serial.TimeBlock> orderedTimeBlocks(Map<Integer, Serial.TimeBlock> timeBlocks) {
    if (timeBlocks.isEmpty()) {
      return ImmutableList.of();
    }
    ImmutableList.Builder<Serial.TimeBlock> builder = ImmutableList.builder();
    Serial.TimeBlock current = firstTimeBlock(timeBlocks);
    builder.add(current);
    checkArgument(!current.hasPrevTime(), "Inconsistent next/prev properties for time block %s",
        current);
    while (current.hasNextTime()) {
      Serial.TimeBlock next = get(timeBlocks, current.getNextTime());
      checkArgument(next.hasPrevTime() && next.getPrevTime() == current.getBlockId(),
          "Inconsistent next/prev properties for time blocks %s, %s", current, next);
      builder.add(next);
      current = next;
    }
    List<Serial.TimeBlock> result = builder.build();
    checkArgument(result.size() == timeBlocks.size(), "Disconnected time blocks");
    // TODO: how do we work out MIT-style, multiple-day Splashes?
    return result;
  }

  public Teacher getTeacher(int teacherId) {
    return get(teachers, teacherId);
  }

  public Course getCourse(int courseId) {
    return get(courses, courseId);
  }

  public RoomProperty getProperty(int propId) {
    return get(roomProperties, propId);
  }

  public TimeBlock getTimeBlock(int blockId) {
    return get(timeBlocks, blockId);
  }

  public Room getRoom(int roomId) {
    return get(rooms, roomId);
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

  private static <T extends HasUID> ImmutableMap<Integer, T> makeIdMap(Collection<T> collection) {
    Map<Integer, T> map = Maps.newLinkedHashMap();
    for (T t : collection) {
      set(map, t.getId(), t);
    }
    return ImmutableMap.copyOf(map);
  }

  public List<Teacher> getTeachers() {
    return teachers.values().asList();
  }

  public List<TimeBlock> getTimeBlocks() {
    return timeBlocks.values().asList();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(teachers, rooms, courses, roomProperties, timeBlocks);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof Program) {
      Program other = (Program) obj;
      return teachers.equals(other.teachers) && rooms.equals(other.rooms)
          && courses.equals(other.courses) && roomProperties.equals(other.roomProperties)
          && timeBlocks.equals(other.timeBlocks);
    }
    return false;
  }

  @Override
  public String toString() {
    return TextFormat.printToString(serial);
  }
}
