package org.learningu.scheduling;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
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

  Course getCourse();

  Section getSection();

  Room getRoom();

  ClassPeriod getPeriod();

  Program getProgram();
}
