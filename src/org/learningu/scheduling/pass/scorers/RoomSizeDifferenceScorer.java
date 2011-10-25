package org.learningu.scheduling.pass.scorers;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.StartAssignment;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.optimization.Scorer;

/**
 * Scorer that penalizes large ratios from room size to class caps at a cubic rate: a ratio of 3
 * versus a ratio of 2 will cost (27 - 8) = 19 more.
 * 
 * @author lowasser
 */
public final class RoomSizeDifferenceScorer implements Scorer<Schedule> {

  @Override
  public double score(Schedule input) {
    double total = 0.0;
    int count = 0;
    for (StartAssignment assign : input.startAssignments()) {
      int roomCapacity = assign.getRoom().getCapacity();
      Course course = assign.getCourse();
      int classCap = course.getMaxClassSize();
      int periodLength = course.getPeriodLength();
      double ratio = (double) roomCapacity / classCap;
      total += (1.0 - (ratio * ratio * ratio)) * periodLength;
      count += periodLength;
    }
    return total / count;
  }
}
