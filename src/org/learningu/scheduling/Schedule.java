package org.learningu.scheduling;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

import com.google.common.collect.Table;

public abstract class Schedule {
  public abstract Program getProgram();

  public abstract Table<ClassPeriod, Room, Section> getStartingTimeTable();
  
  public abstract Table<ClassPeriod, Room, Section> getScheduleTable();
}
