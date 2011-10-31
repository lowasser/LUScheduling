package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.Set;

import org.learningu.scheduling.graph.SerialGraph.SerialSection;

/**
 * A course in an LU program.
 * 
 * @author lowasser
 */
public final class Section extends ProgramObject<SerialSection> implements Comparable<Section> {

  Section(Program program, SerialSection serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getSectionId();
  }

  public Course getCourse() {
    return new Course(serial.getClassId(), getTitle(), getProgram());
  }

  public int getPeriodLength() {
    return serial.getPeriodLength();
  }

  public String getTitle() {
    return serial.getCourseTitle();
  }

  Set<Course> getPrerequisites() {
    return ImmutableSet.copyOf(Lists.transform(
        serial.getPrereqCourseIdsList(),
        Functions.forMap(program.courses)));
  }

  // Does not cache!
  Set<Teacher> getTeachers() {
    return ImmutableSet.copyOf(Lists.transform(
        serial.getTeacherIdsList(),
        Functions.forMap(program.teachers)));
  }

  Set<RoomProperty> getRequiredProperties() {
    return ImmutableSet.copyOf(Lists.transform(
        serial.getRequiredPropertyList(),
        Functions.forMap(program.roomProperties)));
  }

  public int getEstimatedClassSize() {
    return serial.getEstimatedClassSize();
  }

  public int getMaxClassSize() {
    return serial.getMaxClassSize();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", getId()).toString();
  }

  static Function<SerialSection, Section> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<SerialSection, Section>() {
      @Override
      public Section apply(SerialSection input) {
        return new Section(program, input);
      }
    };
  }

  @Override
  public int compareTo(Section o) {
    return Ints.compare(getId(), o.getId());
  }
}
