package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

public final class Course extends ProgramObject<Serial.Course> {

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

  public int getNumberOfSections() {
    return serial.getSections();
  }

  private transient Set<RoomProperty> roomRequiredProperties;

  public Set<RoomProperty> getRoomRequiredProperties() {
    Set<RoomProperty> result = roomRequiredProperties;
    if (result != null) {
      return result;
    }
    ImmutableSet.Builder<RoomProperty> builder = ImmutableSet.builder();
    for (int propId : serial.getRoomRequiredPropertiesList()) {
      builder.add(program.getProperty(propId));
    }
    return roomRequiredProperties = builder.build();
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
