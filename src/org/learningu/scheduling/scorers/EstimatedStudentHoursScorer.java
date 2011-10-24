package org.learningu.scheduling.scorers;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.StartAssignment;
import org.learningu.scheduling.optimization.Scorer;

public final class EstimatedStudentHoursScorer implements Scorer<Schedule> {

  @Override
  public double score(Schedule input) {
    long total = 0;
    for (StartAssignment assign : input.startAssignments()) {
      total += assign.getCourse().getEstimatedClassSize() * assign.getCourse().getPeriodLength();
    }
    return total;
  }

}
