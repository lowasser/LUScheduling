package org.learningu.scheduling;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.collect.Table;

public abstract class Schedule {
  public abstract ProgramScheduler getScheduler();

  public Program getProgram() {
    return getScheduler().getProgram();
  }

  public abstract Table<TimeBlock, Room, Course> getScheduleTable();
}
