package org.learningu.scheduling.graph;

import com.google.common.base.Objects;

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

  @Override
  public int hashCode() {
    return id ^ System.identityHashCode(getProgram());
  }

  public Program getProgram() {
    return prototype.getProgram();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof Course) {
      Course c = (Course) obj;
      return id == c.id && getProgram() == c.getProgram();
    }
    return false;
  }

  public Subject getSubject(){
    return prototype.getSubject();
  }
  
  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", id).add("title", getTitle()).toString();
  }

  public Set<Teacher> getTeachers() {
    return prototype.getTeachers();
  }

  public Set<Resource> getRequiredResources() {
    return prototype.getRequiredResources();
  }
}
