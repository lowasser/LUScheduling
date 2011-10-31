package org.learningu.scheduling.graph;

import com.google.common.base.Objects;

import javax.annotation.Nullable;

public final class Course {
  private final int id;
  private final String title;
  private final Program program;

  Course(int id, String title, Program program) {
    this.id = id;
    this.title = title;
    this.program = program;
  }

  public int getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return id ^ System.identityHashCode(program);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof Course) {
      Course c = (Course) obj;
      return id == c.id && program == c.program;
    }
    return false;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", id).add("title", title).toString();
  }
}
