package org.learningu.scheduling;

import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.collect.Table;

public interface Schedule {
  Program getProgram();
  
  Table<TimeBlock, Room, Section> getScheduleTable();
}
