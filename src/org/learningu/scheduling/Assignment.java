package org.learningu.scheduling;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

public interface Assignment {
  StartAssignment getStartAssignment();

  Course getCourse();

  Section getSection();

  Room getRoom();

  ClassPeriod getPeriod();

  Program getProgram();
}
