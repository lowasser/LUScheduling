package org.learningu.scheduling;

import java.util.Map.Entry;
import java.util.NavigableMap;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.util.ImmutableNavigableMap;

import com.google.common.collect.ImmutableMap;

public final class ImmutableSchedule extends Schedule {

  public static ImmutableSchedule copyOf(Schedule schedule) {
    if (schedule instanceof ImmutableSchedule) {
      return (ImmutableSchedule) schedule;
    }
    ImmutableMap.Builder<Room, ImmutableNavigableMap<ClassPeriod, Section>> builder = ImmutableMap
        .builder();
    for (Entry<Room, ? extends NavigableMap<ClassPeriod, Section>> entry : schedule.startingTimeTable
        .entrySet()) {
      builder.put(entry.getKey(), ImmutableNavigableMap.copyOf(entry.getValue()));
    }
    return new ImmutableSchedule(schedule.getProgram(), builder.build());
  }

  private ImmutableSchedule(Program program,
      ImmutableMap<Room, ImmutableNavigableMap<ClassPeriod, Section>> startingTimeTable) {
    super(program, startingTimeTable);
  }

}
