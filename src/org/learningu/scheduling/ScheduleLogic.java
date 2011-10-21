package org.learningu.scheduling;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;

public interface ScheduleLogic {
  boolean isCompatible(Course c, TimeBlock b);

  boolean isCompatible(Course c, Room r);

  boolean isCompatible(Room r, TimeBlock b);
  
  boolean isCompatible(Teacher t, TimeBlock b);
}
