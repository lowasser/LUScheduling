package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.learningu.scheduling.graph.Serial.SerialCourse;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * A course in an LU program.
 * 
 * @author lowasser
 */
public final class Course extends ProgramObject<SerialCourse> {
  /*
   * Features that might be added in the future include: prerequisites, multi-block classes.
   */

  Course(Program program, SerialCourse serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getCourseId();
  }

  public String getTitle() {
    return serial.getCourseTitle();
  }

  // Does not cache!
  Set<Teacher> getTeachers() {
    return ProgramObjectSet.create(Lists.transform(serial.getTeacherIdsList(),
        program.teachers.asLookupFunction()));
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

  static Function<SerialCourse, Course> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<SerialCourse, Course>() {
      @Override
      public Course apply(SerialCourse input) {
        return new Course(program, input);
      }
    };
  }
}
