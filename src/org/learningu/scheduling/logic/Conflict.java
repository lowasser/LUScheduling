package org.learningu.scheduling.logic;

import org.learningu.scheduling.Assignment;

/**
 * A conflict stopping a schedule assignment from being made.
 * 
 * @author lowasser
 */
public interface Conflict<C extends Assignment> {
  C getCandidateAssignment();

  String getFailedCondition();
}
