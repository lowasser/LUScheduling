package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

/**
 * A course in an LU program.
 * 
 * @author lowasser
 */
public final class Course extends ProgramObject<Serial.Course> {
  /*
   * Features that might be added in the future include: prerequisites, multi-block classes.
   */

  Course(Program program, org.learningu.scheduling.Serial.Course serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getCourseId();
  }

  private transient Set<Teacher> teachers;

  public Set<Teacher> getTeachers() {
    Set<Teacher> result = teachers;
    if (result != null) {
      return result;
    }
    ImmutableSet.Builder<Teacher> builder = ImmutableSet.builder();
    for (int teacherId : serial.getTeacherIdsList()) {
      builder.add(program.getTeacher(teacherId));
    }
    return teachers = builder.build();
  }

  public int getEstimatedClassSize() {
    return serial.getEstimatedClassSize();
  }

  public int getMaxClassSize() {
    return serial.getMaxClassSize();
  }

  public boolean isCompatibleWithRoom(Room room) {
    checkArgument(program.getRooms().contains(room));
    return getMaxClassSize() <= room.getCapacity();
  }

  public boolean isCompatibleWithTimeBlock(TimeBlock block) {
    checkArgument(program.getTimeBlocks().contains(block));
    for (Teacher t : getTeachers()) {
      if (!t.isCompatibleWithTimeBlock(block)) {
        return false;
      }
    }
    return true;
  }

  static Function<Serial.Course, Course> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<Serial.Course, Course>() {
      @Override
      public Course apply(org.learningu.scheduling.Serial.Course input) {
        return new Course(program, input);
      }
    };
  }
}
