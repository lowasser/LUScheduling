package org.learningu.scheduling;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Serial.SerialSchedule;
import org.learningu.scheduling.graph.Serial.SerialSchedule.SerialScheduleAssignment;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;

public class Schedules {
  private Schedules() {
  }

  public static SerialSchedule serialize(Schedule schedule) {
    SerialSchedule.Builder builder = SerialSchedule.newBuilder();
    for (Cell<TimeBlock, Room, Course> cell : schedule.getScheduleTable().cellSet()) {
      builder.addAssignments(serialize(cell));
    }
    return builder.build();
  }

  private static SerialScheduleAssignment serialize(Table.Cell<TimeBlock, Room, Course> cell) {
    SerialScheduleAssignment.Builder builder = SerialScheduleAssignment.newBuilder();
    builder.setTimeBlockId(cell.getRowKey().getId());
    builder.setRoomId(cell.getColumnKey().getId());
    builder.setCourseId(cell.getValue().getId());
    return builder.build();
  }

  public static Schedule deserialize(SerialSchedule schedule, Program program) {
    ImmutableTable.Builder<TimeBlock, Room, Course> builder = ImmutableTable.builder();
    for (SerialScheduleAssignment assignment : schedule.getAssignmentsList()) {
      builder.put(deserialize(assignment, program));
    }
    return new ImmutableSchedule(program, builder.build());
  }

  private static Table.Cell<TimeBlock, Room, Course> deserialize(
      SerialScheduleAssignment assignment,
      Program program) {
    return Tables.immutableCell(
        program.getTimeBlock(assignment.getTimeBlockId()),
        program.getRoom(assignment.getRoomId()),
        program.getCourse(assignment.getCourseId()));
  }
}
