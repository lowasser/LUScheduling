package org.learningu.scheduling.logic;

import org.learningu.scheduling.Assignment;

import com.google.common.base.Objects;

/**
 * A schedule conflict that invalidates a single period/room/section assignment, not caused by a
 * conflict between other schedule assignments, but rather because this period, room, and section
 * are mutually incompatible.
 * 
 * @author lowasser
 */
public final class LocalConflict<C extends Assignment> implements Conflict<C> {
  public static <C extends Assignment> LocalConflict<C> create(C candidateAssignment,
      String failedCondition) {
    return new LocalConflict<C>(candidateAssignment, failedCondition);
  }

  private final C candidateAssignment;
  private final String failedCondition;

  private LocalConflict(C badAssignment, String failedCondition) {
    this.candidateAssignment = badAssignment;
    this.failedCondition = failedCondition;
  }

  @Override
  public C getCandidateAssignment() {
    return candidateAssignment;
  }

  @Override
  public String getFailedCondition() {
    return failedCondition;
  }

  @Override
  public String toString() {
    return Objects
        .toStringHelper(this)
        .add("candidateAssignment", candidateAssignment)
        .add("failedCondition", failedCondition)
        .toString();
  }
}
