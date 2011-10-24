package org.learningu.scheduling.perturbers;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.learningu.scheduling.MutableSchedule;
import org.learningu.scheduling.StartAssignment;
import org.learningu.scheduling.logic.ScheduleValidator;
import org.learningu.scheduling.optimization.Perturber;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * A perturber that attempts to swap classes with the same length. Does not schedule any new
 * classes, but might optimize room assignments.
 * 
 * @author lowasser
 */
public final class SwappingPerturber implements Perturber<MutableSchedule> {
  private final Random gen;

  SwappingPerturber(Random gen) {
    this.gen = gen;
  }

  private <E> E getRandom(List<E> list) {
    checkArgument(!list.isEmpty());
    return list.get(gen.nextInt(list.size()));
  }

  @Override
  public MutableSchedule perturb(MutableSchedule schedule, double temperature) {
    ListMultimap<Integer, StartAssignment> assignedWithLength = ArrayListMultimap.create();
    for (StartAssignment assign : schedule.startAssignments()) {
      assignedWithLength.put(assign.getCourse().getPeriodLength(), assign);
    }
    for (Entry<Integer, Collection<StartAssignment>> assignsWithLength : assignedWithLength
        .asMap()
        .entrySet()) {
      List<StartAssignment> assigns = (List<StartAssignment>) assignsWithLength.getValue();
      int iterations = (int) (assigns.size() * temperature);
      for (int i = 0; i < iterations; i++) {
        StartAssignment a = getRandom(assigns);
        StartAssignment b = getRandom(assigns);
        if (a.equals(b)) {
          continue;
        }
        StartAssignment removedA = schedule.removeStartingAt(a).get();
        assert a.equals(removedA);
        StartAssignment removedB = schedule.removeStartingAt(b).get();
        assert b.equals(removedB);
        ScheduleValidator aToB = schedule.putAssignment(StartAssignment.create(
            b.getPeriod(),
            b.getRoom(),
            a.getSection()));
        if (aToB.isValid()) {
          ScheduleValidator bToA = schedule.putAssignment(StartAssignment.create(
              a.getPeriod(),
              a.getRoom(),
              b.getSection()));
          if (bToA.isValid()) {
            continue;
          }
        }
        schedule.removeStartingAt(a);
        schedule.removeStartingAt(b);
        schedule.putAssignment(a);
        schedule.putAssignment(a);
      }
    }
    return schedule;
  }
}
