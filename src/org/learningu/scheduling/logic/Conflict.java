package org.learningu.scheduling.logic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

/**
 * A conflict between one "main" schedule assignment and several others, which could theoretically
 * be resolved by removing each of the conflicting assignments;
 * 
 * @author lowasser
 * 
 */
public class Conflict {
  public static Conflict create(
      Cell<ClassPeriod, Room, Section> assignment,
      Iterable<? extends Cell<ClassPeriod, Room, Section>> conflictingAssignments,
      String explanation) {
    return new Conflict(explanation, assignment, conflictingAssignments);
  }

  private final String explanation;
  private final Table.Cell<ClassPeriod, Room, Section> assignment;
  private final Collection<Table.Cell<ClassPeriod, Room, Section>> conflictingAssignments;

  private Conflict(
      String explanation,
      Cell<ClassPeriod, Room, Section> assignment,
      Iterable<? extends Cell<ClassPeriod, Room, Section>> conflictingAssignments) {
    this.assignment = checkNotNull(assignment);
    this.explanation = checkNotNull(explanation);
    this.conflictingAssignments = ImmutableSet.copyOf(conflictingAssignments);
  }

  public Cell<ClassPeriod, Room, Section> getAssignment() {
    return assignment;
  }

  public Collection<Cell<ClassPeriod, Room, Section>> getConflictingAssignments() {
    return conflictingAssignments;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("assignment", assignment)
        .add("conflictingAssignments", conflictingAssignments)
        .add("reason", explanation)
        .toString();
  }
}
