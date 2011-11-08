package org.learningu.scheduling.graph;

import com.google.common.base.Objects;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public final class Course {
  private final int id;
  private final Section prototype;

  Course(int id, Section prototype) {
    this.id = id;
    this.prototype = prototype;
  }

  public int getId() {
    return id;
  }

  public String getTitle() {
    return prototype.getTitle();
  }

  public Program getProgram() {
    return prototype.getProgram();
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof Course) {
      Course c = (Course) obj;
      return id == c.id && getProgram() == c.getProgram();
    }
    return false;
  }

  public Subject getSubject() {
    return prototype.getSubject();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", id).add("title", getTitle()).toString();
  }

  public List<Teacher> getTeachers() {
    return prototype.getTeachers();
  }

  public Set<Resource> getRequiredResources() {
    return prototype.getRequiredResources();
  }

  public Range<Integer> getGradeRange() {
    return Ranges.closed(prototype.serial.getMinGrade(), prototype.serial.getMaxGrade());
  }

  public int getPeriodLength() {
    return prototype.getPeriodLength();
  }

  public List<Course> getPrerequisites() {
    return prototype.getPrerequisites();
  }
}
