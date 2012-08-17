package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.List;
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
    return new Course(serial.getCourseId(), this);
  }

  public int getPeriodLength() {
    return serial.getPeriodLength();
  }

  public String getTitle() {
    return serial.getCourseTitle();
  }

  List<Course> getPrerequisites() {
    return ImmutableList.copyOf(Lists.transform(
        serial.getPrereqCourseIdList(),
        Functions.forMap(program.courses)));
  }

  // Does not cache!
  List<Teacher> getTeachers() {
    return ImmutableList.copyOf(Lists.transform(
        serial.getTeacherIdList(),
        Functions.forMap(program.teachers)));
  }

  Set<Resource> getRequiredResources() {
    return ImmutableSet.copyOf(Lists.transform(
        serial.getRequiredResourceList(),
        Functions.forMap(program.resources)));
  }

  public Optional<Room> getPreferredRoom() {
    if (serial.hasPreferredRoom() && serial.getPreferredRoom() != -1) {
      return Optional.of(program.getRoom(serial.getPreferredRoom()));
    } else {
      return Optional.absent();
    }
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

  public Subject getSubject() {
    return checkNotNull(getProgram().subjects.get(serial.getSubjectId()));
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

  @Override
  public String getShortDescription() {
    return String.format("%s (section %d)", getTitle(), getId());
  }
}
