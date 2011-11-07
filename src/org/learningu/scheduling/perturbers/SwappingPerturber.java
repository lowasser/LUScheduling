package org.learningu.scheduling.perturbers;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.List;
import java.util.Random;

import org.learningu.scheduling.optimization.Perturber;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

final class SwappingPerturber implements Perturber<Schedule> {
  private final Random rand;

  @Inject
  SwappingPerturber(Random rand) {
    this.rand = rand;
  }

  @Override
  public Schedule perturb(Schedule initial, double temperature) {
    List<StartAssignment> assigns = Lists.newArrayList(initial.getStartAssignments());
    if (assigns.isEmpty()) {
      return initial;
    }
    int stop = (int) (50 * temperature);
    Schedule current = initial;
    for (int k = 0; k <= stop && !assigns.isEmpty(); k++) {
      int i = rand.nextInt(assigns.size());
      StartAssignment a = assigns.remove(i);
      if (assigns.isEmpty()) {
        break;
      }
      int j = rand.nextInt(assigns.size());
      StartAssignment b = assigns.remove(j);
      Schedule removed = current
          .removeStartingAt(a.getPeriod(), a.getRoom())
          .getNewState()
          .removeStartingAt(b.getPeriod(), b.getRoom())
          .getNewState();
      current = removed;
      try {
        current = current.assignStart(
            StartAssignment.create(a.getPeriod(), a.getRoom(), b.getSection())).getNewState();
      } catch (IllegalArgumentException e) {
        // continue
      }
      try {
        current = current.assignStart(
            StartAssignment.create(b.getPeriod(), b.getRoom(), a.getSection())).getNewState();
      } catch (IllegalArgumentException e) {
        // continue
      }
    }
    return current;
  }
}
