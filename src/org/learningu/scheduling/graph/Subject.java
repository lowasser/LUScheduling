package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Objects;

import javax.annotation.Nullable;

import org.learningu.scheduling.graph.SerialGraph.SerialSubject;

public final class Subject extends ProgramObject<SerialSubject> {
  private Subject(Program program, SerialSubject serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getSubjectId();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", getId()).add("title", getTitle()).toString();
  }

  public String getTitle() {
    return serial.getTitle();
  }

  @Override
  public int hashCode() {
    return getId() ^ 1298301238;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof Subject) {
      Subject other = (Subject) obj;
      return getId() == other.getId() && getProgram() == other.getProgram();
    }
    return false;
  }

  static Function<SerialSubject, Subject> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<SerialSubject, Subject>() {
      @Override
      public Subject apply(SerialSubject input) {
        return new Subject(program, input);
      }
    };
  }

  @Override
  public String getShortDescription() {
    return getTitle();
  }

}
