package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Map;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

/**
 * A mutable schedule for a LU program. Throws on an attempt to reschedule a current scheduled
 * class or overlap rooms.
 * 
 * @author lowasser
 */
public final class MutableSchedule extends Schedule {
  final Program program;
  final Table<TimeBlock, Room, ScheduleAssignment> scheduleTable;
  final Map<Course, ScheduleAssignment> courseAssignment;

  public static MutableSchedule create(Program program) {
    return new MutableSchedule(checkNotNull(program),
        ArrayTable.<TimeBlock, Room, ScheduleAssignment> create(
            program.getTimeBlocks(), program.getRooms()),
        Maps.<Course, ScheduleAssignment> newHashMap());
  }

  public static MutableSchedule create(Schedule schedule) {
    checkNotNull(schedule);
    return new MutableSchedule(schedule.getProgram(), HashBasedTable.create(schedule
        .getScheduleTable()), Maps.newHashMap(schedule.getAssignmentsForCourses()));
  }

  private MutableSchedule(Program program,
      Table<TimeBlock, Room, ScheduleAssignment> scheduleTable,
      Map<Course, ScheduleAssignment> courseAssignment) {
    this.program = program;
    this.scheduleTable = scheduleTable;
    this.courseAssignment = courseAssignment;
  }

  public void remove(ScheduleAssignment assignment) {
    checkNotNull(assignment);
    checkArgument(courseAssignment.get(assignment.getCourse()).equals(assignment));
    scheduleTable.remove(assignment.getTimeBlock(), assignment.getRoom());
    courseAssignment.remove(assignment.getCourse());
  }

  public void assign(ScheduleAssignment assignment) {
    checkNotNull(assignment);
    checkState(!scheduleTable.contains(assignment.getTimeBlock(), assignment.getRoom()));
    checkState(!courseAssignment.containsKey(assignment.getCourse()));
    courseAssignment.put(assignment.getCourse(), assignment);
    scheduleTable.put(assignment.getTimeBlock(), assignment.getRoom(), assignment);
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
}
