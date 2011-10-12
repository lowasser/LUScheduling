package org.learningu.scheduling;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class Program {
  private final ImmutableMap<Integer, Teacher> teachers;
  private final ImmutableMap<Integer, Course> courses;
  private final ImmutableMap<Integer, RoomProperty> roomProperties;
  private final ImmutableMap<Integer, TimeBlock> timeBlocks;
  private final ImmutableMap<Integer, Room> rooms;
  private final ImmutableSetMultimap<Teacher, Course> teachingMap;
  private final ImmutableMap<TimeBlock, Integer> timeBlockIndex;

  Program(Serial.ProgramParameters program) {
    teachers = makeIdMap(Lists.transform(program.getTeachersList(), Teacher.programWrapper(this)));
    courses = makeIdMap(Lists.transform(program.getCoursesList(), Course.programWrapper(this)));
    roomProperties = makeIdMap(Lists.transform(program.getRoomPropertiesList(),
        RoomProperty.programWrapper(this)));
    rooms = makeIdMap(Lists.transform(program.getRoomsList(), Room.programWrapper(this)));

    // initialize timeBlocks
    List<Serial.TimeBlock> orderedBlocks = orderedTimeBlocks(Maps.uniqueIndex(
        program.getTimeBlocksList(), new Function<Serial.TimeBlock, Integer>() {

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
    ImmutableList.Builder<Serial.TimeBlock> builder = ImmutableList.builder();
    Serial.TimeBlock current = firstTimeBlock(timeBlocks);
    builder.add(current);
    assert !current.hasPrevTime();
    while (current.hasNextTime()) {
      Serial.TimeBlock next = get(timeBlocks, current.getNextTime());
      assert next.hasPrevTime() && next.getPrevTime() == current.getBlockId();
      builder.add(next);
      current = next;
    }
    return builder.build();
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
    if (value == null) {
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
}
