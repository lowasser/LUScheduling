package org.learningu.scheduling.graph;

import com.google.common.base.Objects;

import javax.annotation.Nullable;

public final class TeacherGroup {
  private final int groupId;
  private final Program program;

  TeacherGroup(int groupId, Program program) {
    this.groupId = groupId;
    this.program = program;
  }

  public int getId() {
    return groupId;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof TeacherGroup) {
      TeacherGroup other = (TeacherGroup) obj;
      return groupId == other.groupId && program == other.program;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return groupId;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("groupId", groupId).toString();
  }
}
