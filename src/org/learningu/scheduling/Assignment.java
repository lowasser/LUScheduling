package org.learningu.scheduling;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;

/**
 * An assignment that some particular class will be in the specified room at the specified period.
 * 
 * @author lowasser
 */
public interface Assignment {
  StartAssignment getStartAssignment();

  Section getCourse();

  Room getRoom();

  ClassPeriod getPeriod();

  Program getProgram();
}
