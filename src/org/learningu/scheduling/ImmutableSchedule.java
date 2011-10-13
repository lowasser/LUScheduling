package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

/**
 * An immutable schedule for an LU program.
 * 
 * @author lowasser
 */
public final class ImmutableSchedule extends Schedule {
  private final Program program;
  private final ImmutableTable<TimeBlock, Room, ScheduleAssignment> scheduleTable;
  private final ImmutableMap<Course, ScheduleAssignment> courseAssignment;

  public static ImmutableSchedule copyOf(Schedule schedule) {
    checkNotNull(schedule);
    return new ImmutableSchedule(schedule.getProgram(), ImmutableTable.copyOf(schedule
        .getScheduleTable()), ImmutableMap.copyOf(schedule.getAssignmentsForCourses()));
  }

  private ImmutableSchedule(Program program,
      ImmutableTable<TimeBlock, Room, ScheduleAssignment> scheduleTable,
      ImmutableMap<Course, ScheduleAssignment> courseAssignment) {
    this.program = program;
    this.scheduleTable = scheduleTable;
    this.courseAssignment = courseAssignment;
  }

  @Override
  public Program getProgram() {
    return program;
  }

  @Override
  protected Table<TimeBlock, Room, ScheduleAssignment> getScheduleTable() {
    return scheduleTable;
  }

  @Override
  public Map<Course, ScheduleAssignment> getAssignmentsForCourses() {
    return courseAssignment;
  }

  @Override
  public void remove(ScheduleAssignment assignment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void assign(ScheduleAssignment assignment) {
    throw new UnsupportedOperationException();
  }
}
