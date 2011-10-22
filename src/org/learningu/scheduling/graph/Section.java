package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

public final class Section {
  private final Course course;
  private final int section;

  Section(Course course, int section) {
    this.course = checkNotNull(course);
    this.section = section;
    checkArgument(section >= 0 && section < course.getSectionCount());
  }

  public Program getProgram() {
    return course.getProgram();
  }

  public Course getCourse() {
    return course;
  }

  public int getSection() {
    return section;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(course, section);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof Section) {
      Section s = (Section) obj;
      return course.equals(s.course) && section == s.section;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("course", course).add("section", section).toString();
  }

}
