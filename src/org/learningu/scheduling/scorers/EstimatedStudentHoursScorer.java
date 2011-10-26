package org.learningu.scheduling.scorers;

import org.learningu.scheduling.optimization.Scorer;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

public final class EstimatedStudentHoursScorer implements Scorer<Schedule> {

  @Override
  public double score(Schedule input) {
    long total = 0;
    for (StartAssignment assign : input.startAssignments()) {
      total += assign.getSection().getEstimatedClassSize() * assign.getSection().getPeriodLength();
    }
    return total;
  }

}
