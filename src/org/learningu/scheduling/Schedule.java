package org.learningu.scheduling;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;

/**
 * A (potentially mutable) view of a schedule at an LU program.
 * 
 * @author lowasser
 */
public abstract class Schedule {
  public abstract Program getProgram();

  protected abstract Table<TimeBlock, Room, ScheduleAssignment> getScheduleTable();

  public abstract Map<Course, ScheduleAssignment> getAssignmentsForCourses();

  public Map<Room, ScheduleAssignment> getAssignmentsAtTime(TimeBlock block) {
    return Collections.unmodifiableMap(getScheduleTable().row(block));
  }

  public Map<TimeBlock, ScheduleAssignment> getAssignmentsForRoom(Room room) {
    return Collections.unmodifiableMap(getScheduleTable().column(room));
  }

  public abstract void remove(ScheduleAssignment assignment);

  public abstract void assign(ScheduleAssignment assignment);

  public void assign(TimeBlock timeBlock, Room room, Course course) {
    assign(ScheduleAssignment.createAssignment(course, timeBlock, room));
  }
}
