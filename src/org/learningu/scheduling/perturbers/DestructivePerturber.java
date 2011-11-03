package org.learningu.scheduling.perturbers;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;
import java.util.Random;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.optimization.Perturber;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

final class DestructivePerturber implements Perturber<Schedule> {
  private final Random rand;

  @Inject
  DestructivePerturber(Random rand) {
    this.rand = rand;
  }

  private <E> E getRandom(List<E> list) {
    assert !list.isEmpty();
    return list.get(rand.nextInt(list.size()));
  }

  @Override
  public Schedule perturb(Schedule initial, double temperature) {
    Program program = initial.getProgram();
    List<Section> sections = ImmutableList.copyOf(program.getSections());
    List<Room> rooms = ImmutableList.copyOf(program.getRooms());
    List<ClassPeriod> periods = ImmutableList.copyOf(program.getPeriods());

    int nAttempts = Math.max(1, (int) (sections.size() * temperature));

    Schedule current = initial;
    for (int i = 0; i < nAttempts; i++) {
      Section section = getRandom(sections);
      Room room = getRandom(rooms);
      ClassPeriod period = getRandom(periods);

      try {
        current = current
            .forceAssignStart(StartAssignment.create(period, room, section))
            .getNewState();
      } catch (IllegalArgumentException e) {
        continue;
      }
    }

    return current;
  }

}
