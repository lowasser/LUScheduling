package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

import org.learningu.scheduling.graph.SerialGraph.SerialTeacher;

/**
 * A teacher at an LU program.
 * 
 * @author lowasser
 */
public final class Teacher extends ProgramObject<SerialTeacher> {

  Teacher(Program program, SerialTeacher serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getTeacherId();
  }

  // Does not cache!
  Set<ClassPeriod> getCompatiblePeriods() {
    return ImmutableSet.copyOf(Lists.transform(
        serial.getAvailablePeriodList(),
        Functions.forMap(program.periods)));
  }

  public String getName() {
    return serial.getName();
  }

  @Override
  public String toString() {
    return serial.hasName() ? serial.getName() : super.toString();
  }

  public List<TeacherGroup> getTeacherGroups() {
    return Lists.transform(serial.getGroupIdList(), Functions.forMap(program.teacherGroups));
  }

  static Function<SerialTeacher, Teacher> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<SerialTeacher, Teacher>() {
      @Override
      public Teacher apply(SerialTeacher input) {
        return new Teacher(program, input);
      }
    };
  }
}
