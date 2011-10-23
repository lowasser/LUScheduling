package org.learningu.scheduling.logic;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.learningu.scheduling.Assignment;

import com.google.common.collect.Iterables;

/**
 * A conflict between a candidate assignment and several assignments already in the schedule, which
 * could in principle be resolved by deleting each of the specified conflicting assignments.
 * 
 * @author lowasser
 */
public final class GlobalConflict<C extends Assignment> implements Conflict<C> {
  public static <C extends Assignment> GlobalConflict<C> create(C candidateAssignment,
      Iterable<C> conflictingAssignments, String failedCondition) {
    return new GlobalConflict<C>(candidateAssignment, conflictingAssignments, failedCondition);
  }

  private final C candidateAssignment;
  private final Iterable<C> conflictingAssignments;
  private final String failedCondition;

  private GlobalConflict(C candidateAssignment, Iterable<C> conflictingAssignments,
      String failedCondition) {
    this.candidateAssignment = checkNotNull(candidateAssignment);
    this.conflictingAssignments = checkNotNull(conflictingAssignments);
    this.failedCondition = checkNotNull(failedCondition);
    checkArgument(
        !Iterables.isEmpty(conflictingAssignments),
        "No conflicting assignments when creating global conflict for %s with failed condition %s",
        candidateAssignment,
        failedCondition);
  }

  @Override
  public C getCandidateAssignment() {
    return candidateAssignment;
  }

  public Iterable<C> getConflictingAssignments() {
    return conflictingAssignments;
  }

  @Override
  public String getFailedCondition() {
    return failedCondition;
  }
}
