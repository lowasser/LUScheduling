package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.learningu.scheduling.graph.SerialGraph.SerialSection;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

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

  public int getPeriodLength() {
    return serial.getPeriodLength();
  }

  public String getTitle() {
    return serial.getCourseTitle();
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
    return serial.hasCourseTitle() ? getTitle() : super.toString();
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
