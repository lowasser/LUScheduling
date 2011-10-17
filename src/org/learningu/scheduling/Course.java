package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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

  public String getTitle() {
    return serial.getCourseTitle();
  }

  private transient Set<Teacher> teachers;

  public Set<Teacher> getTeachers() {
    Set<Teacher> result = teachers;
    if (result != null) {
      return result;
    }
    return teachers = ProgramObjectSet.create(Lists.transform(serial.getTeacherIdsList(),
        program.getTeachers().asLookupFunction()));
  }

  public int getEstimatedClassSize() {
    return serial.getEstimatedClassSize();
  }

  public int getMaxClassSize() {
    return serial.getMaxClassSize();
  }

  @Override
  public String toString() {
    return serial.hasCourseTitle() ? getTitle() : super.toString();
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
