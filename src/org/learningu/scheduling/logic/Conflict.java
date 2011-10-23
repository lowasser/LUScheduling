package org.learningu.scheduling.logic;

import org.learningu.scheduling.Assignment;

public interface Conflict<C extends Assignment> {
  C getCandidateAssignment();

  String getFailedCondition();
}
