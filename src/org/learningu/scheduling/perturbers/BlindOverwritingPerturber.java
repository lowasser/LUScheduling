package org.learningu.scheduling.perturbers;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.learningu.scheduling.MutableSchedule;
import org.learningu.scheduling.StartAssignment;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.optimization.Perturber;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * An extremely dumb perturber that takes all the unscheduled classes and schedules them randomly,
 * overwriting anything in the way.
 * 
 * @author lowasser
 */
public final class BlindOverwritingPerturber implements Perturber<MutableSchedule> {
  private final Random gen;

  @Inject
  BlindOverwritingPerturber(Random gen) {
    this.gen = gen;
  }

  private <E> E getRandom(List<E> list) {
    checkArgument(!list.isEmpty());
    return list.get(gen.nextInt(list.size()));
  }

  @Override
  public MutableSchedule perturb(MutableSchedule schedule, double temperature) {
    Program program = schedule.getProgram();
    Set<Section> unscheduled = Sets.newHashSet(program.getSections());
    for (Section section : schedule.scheduledSections()) {
      unscheduled.remove(section);
    }
    List<Section> unscheduledList = Lists.newArrayList(unscheduled);
    Collections.shuffle(unscheduledList, gen);
    if (unscheduledList.isEmpty()) {
      return schedule;
    }
    unscheduledList = unscheduledList.subList(
        0,
        Math.max(1, (int) (unscheduledList.size() * temperature)));
    List<Room> rooms = ImmutableList.copyOf(program.getRooms());
    List<ClassPeriod> periods = ImmutableList.copyOf(program.getPeriods());
    for (Section section : unscheduledList) {
      Room room = getRandom(rooms);
      ClassPeriod period = getRandom(periods);
      try {
        StartAssignment assign = StartAssignment.create(period, room, section);
        schedule.forcePutAssignment(assign);
        // success or failure, we don't care; we keep attempting to perturb
      } catch (IllegalArgumentException e) {
        // keep trying
      }
    }
    return schedule;
  }
}
