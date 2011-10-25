package org.learningu.scheduling.pass.scorers;

import java.util.Set;

import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.optimization.Scorer;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

import com.google.common.collect.Sets;

/**
 * Scorer that scores schedules based on how many distinct classes were scheduled.
 * 
 * @author lowasser
 */
public final class DistinctClassesScorer implements Scorer<Schedule> {

  @Override
  public double score(Schedule input) {
    Set<Section> scheduled = Sets
        .newHashSetWithExpectedSize(input.getProgram().getSections().size());
    for (StartAssignment assign : input.startAssignments()) {
      scheduled.add(assign.getCourse());
    }
    return scheduled.size();
  }

}
