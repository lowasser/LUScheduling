package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.protobuf.TextFormat;

/**
 * A specification for a LU Splash-type program: all the ``inputs.'' The {@code Program} object is
 * responsible for combining all the serialized program data into an actual object graph, tracking
 * associations between IDs and teachers, courses, rooms, etc.
 * 
 * @author lowasser
 */
public final class Program {
  private final ProgramObjectSet<Teacher> teachers;
  private final ProgramObjectSet<Course> courses;
  private final ProgramObjectSet<TimeBlock> timeBlocks;
  private final ProgramObjectSet<Room> rooms;
  private final ImmutableSetMultimap<Teacher, Course> teachingMap;
  private final Serial.Program serial;

  public Program(Serial.Program serial) {
    this.serial = checkNotNull(serial);
    teachers = ProgramObjectSet.create(Lists.transform(serial.getTeachersList(),
        Teacher.programWrapper(this)));
    courses = ProgramObjectSet.create(Lists.transform(serial.getCoursesList(),
        Course.programWrapper(this)));
    rooms = ProgramObjectSet.create(Lists.transform(serial.getRoomsList(),
        Room.programWrapper(this)));
    timeBlocks = ProgramObjectSet.create(Lists.transform(serial.getTimeBlocksList(),
        TimeBlock.programWrapper(this)));

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
  }

  private void checkTeachersValid() {
    for (Teacher t : teachers) {
      t.getCompatibleTimeBlocks();
    }
  }

  private void checkCoursesValid() {
    for (Course c : courses) {
      checkArgument(c.getEstimatedClassSize() <= c.getMaxClassSize(),
          "Class %s has estimated class size %s > max class size %s", c,
          c.getEstimatedClassSize(), c.getMaxClassSize());
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

  public ProgramObjectSet<Teacher> getTeachers() {
    return teachers;
  }

  public ProgramObjectSet<Course> getCourses() {
    return courses;
  }

  public ProgramObjectSet<TimeBlock> getTimeBlocks() {
    return timeBlocks;
  }

  public ProgramObjectSet<Room> getRooms() {
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
  
  public String getName(){
    return serial.getName();
  }

  @Override
  public String toString() {
    return TextFormat.printToString(serial);
  }
}
