package org.learningu.scheduling.perturbers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.optimization.Perturber;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

public class DestructiveByRoomPerturber implements Perturber<Schedule> {
  private final Random rand;

  @Inject
  DestructiveByRoomPerturber(Random rand) {
    this.rand = rand;
  }

  @Override
  public Schedule perturb(Schedule initial, double temperature) {
    List<Map.Entry<ClassPeriod, Room>> free = Lists.newArrayList();
    Program program = initial.getProgram();
    Schedule current = initial;
    for (Room room : program.getRooms()) {
      for (ClassPeriod period : program.compatiblePeriods(room)) {
        if (!current.occurringAt(period, room).isPresent()) {
          free.add(Maps.immutableEntry(period, room));
        }
      }
    }
    Collections.shuffle(free, rand);
    List<Section> unscheduledSections = Lists.newArrayList(Sets.difference(
        program.getSections(),
        current.getScheduledSections()));
    Collections.shuffle(unscheduledSections, rand);
    int n = (int) (Math.min(unscheduledSections.size(), free.size()) * temperature);
    for (int i = 0; i < n; i++) {
      try {
        current = current.forceAssignStart(
            StartAssignment.create(
                free.get(i).getKey(),
                free.get(i).getValue(),
                unscheduledSections.get(i))).getNewState();
      } catch (IllegalArgumentException e) {
        continue;
      }
    }
    return current;
  }
}
