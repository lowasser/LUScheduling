package org.learningu.scheduling;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;
import org.learningu.scheduling.util.Condition;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultScheduleLogic.class)
public abstract class ScheduleLogic {
  public boolean isCompatible(Course course, TimeBlock block) {
    return course.getProgram() == block.getProgram();
  }

  public boolean isCompatible(Course course, Room room) {
    return course.getProgram() == room.getProgram();
  }

  public boolean isCompatible(Room room, TimeBlock block) {
    return room.getProgram() == block.getProgram();
  }

  public boolean isCompatible(Teacher teacher, TimeBlock block) {
    return teacher.getProgram() == block.getProgram();
  }

  public Condition isValid(Schedule schedule) {
    Condition validSchedule = Condition.create(Logger.getLogger(getClass().toString()), Level.FINE);
    isLocallyValid(validSchedule, schedule);
    return validSchedule;
  }

  private Condition isLocallyValid(Condition parent, Schedule schedule) {
    Table<TimeBlock, Room, Course> table = schedule.getScheduleTable();
    Condition local = parent.createSubCondition("local");
    local.getLogger().log(Level.FINEST, "Testing local validity of schedule: %s", schedule);
    for (Cell<TimeBlock, Room, Course> cell : table.cellSet()) {
      TimeBlock block = cell.getRowKey();
      Room room = cell.getColumnKey();
      Course course = cell.getValue();
      local.verify(
          isCompatible(course, block),
          "Course %s cannot occur in block %s",
          course,
          block);
      local.verify(isCompatible(room, block), "Room %s cannot occur in block %s", room, block);
      local.verify(isCompatible(course, room), "Course %s cannot occur in room %s", course, room);
    }
    return local;
  }
}
