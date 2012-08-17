package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Objects;

import org.learningu.scheduling.graph.SerialGraph.SerialTeacherGroup;

public final class TeacherGroup extends ProgramObject<SerialTeacherGroup> {
  TeacherGroup(Program program, SerialTeacherGroup serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getGroupId();
  }

  @Override
  public int hashCode() {
    return getId();
  }

  public int getCap() {
    return serial.getCap();
  }

  public String getName() {
    return serial.getName();
  }

  @Override
  public String toString() {
    return Objects
        .toStringHelper(this)
        .add("groupId", getId())
        .add("name", serial.getName())
        .toString();
  }

  static Function<SerialTeacherGroup, TeacherGroup> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<SerialTeacherGroup, TeacherGroup>() {
      @Override
      public TeacherGroup apply(SerialTeacherGroup input) {
        return new TeacherGroup(program, input);
      }
    };
  }

  @Override
  public String getShortDescription() {
    return getName();
  }
}
