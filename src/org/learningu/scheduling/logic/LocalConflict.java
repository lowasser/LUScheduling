package org.learningu.scheduling.logic;

import static com.google.common.base.Preconditions.checkNotNull;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

import com.google.common.base.Objects;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

public final class LocalConflict {
  public static LocalConflict create(Cell<ClassPeriod, Room, Section> assignment, String reason) {
    return new LocalConflict(assignment, reason);
  }

  private final Table.Cell<ClassPeriod, Room, Section> assignment;
  private final String reason;

  private LocalConflict(Cell<ClassPeriod, Room, Section> assignment, String reason) {
    this.assignment = checkNotNull(assignment);
    this.reason = checkNotNull(reason);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("assignment", assignment)
        .add("reason", reason)
        .toString();
  }
}
