package org.learningu.scheduling.schedule;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

/**
 * An assignment that some particular class will be in the specified room at the specified period.
 * 
 * @author lowasser
 */
public interface Assignment {
  StartAssignment getStartAssignment();

  Section getSection();

  Room getRoom();

  ClassPeriod getPeriod();

  Program getProgram();
}
