package org.learningu.scheduling;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultScheduleLogic.class)
public interface ScheduleLogic {
  boolean isCompatible(Course course, TimeBlock block);

  boolean isCompatible(Course course, Room room);

  boolean isCompatible(Room room, TimeBlock block);

  boolean isCompatible(Teacher teacher, TimeBlock block);
}
