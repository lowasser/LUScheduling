package org.learningu.scheduling.perturbers;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.optimization.Perturber;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Schedules classes greedily and randomly, without deleting any already-scheduled classes.
 * 
 * @author lowasser
 */
final class GreedyPerturber implements Perturber<Schedule> {
  private final Random rand;

  @Inject
  GreedyPerturber(Random rand) {
    this.rand = rand;
  }

  private <E> E getRandom(List<E> list) {
    assert !list.isEmpty();
    return list.get(rand.nextInt(list.size()));
  }

  @Override
  public Schedule perturb(Schedule initial, double temperature) {
    Program program = initial.getProgram();
    int numberUnscheduled = program.getSections().size() - initial.getScheduledSections().size();
    List<Section> unscheduled = Lists.newArrayListWithCapacity(numberUnscheduled);
    unscheduled.addAll(Sets.difference(program.getSections(), initial.getScheduledSections()));
    Collections.shuffle(unscheduled, rand);

    int nAttempts = Math.max(1, (int) (unscheduled.size() * temperature));
    unscheduled = unscheduled.subList(0, nAttempts);

    ImmutableList<Room> rooms = ImmutableList.copyOf(program.getRooms());
    ImmutableList<ClassPeriod> periods = ImmutableList.copyOf(program.getPeriods());
    Schedule current = initial;
    for (Section section : unscheduled) {
      Room room = getRandom(rooms);
      ClassPeriod pd = getRandom(periods);
      try {
        StartAssignment assign = StartAssignment.create(pd, room, section);
        current = current.assignStart(assign).getNewState();
      } catch (IllegalArgumentException e) {
        // not enough periods left in the block
        continue;
      }
    }

    return current;
  }
}
