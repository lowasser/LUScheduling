package org.learningu.scheduling;

import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Serial.SerialSchedule;
import org.learningu.scheduling.graph.Serial.SerialSchedule.SerialScheduleAssignment;
import org.learningu.scheduling.graph.Serial.SerialSection;
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
    for (Cell<TimeBlock, Room, Section> cell : schedule.getScheduleTable().cellSet()) {
      builder.addAssignments(serialize(cell));
    }
    return builder.build();
  }

  private static SerialScheduleAssignment serialize(Table.Cell<TimeBlock, Room, Section> cell) {
    SerialScheduleAssignment.Builder builder = SerialScheduleAssignment.newBuilder();
    builder.setTimeBlockId(cell.getRowKey().getId());
    builder.setRoomId(cell.getColumnKey().getId());
    builder.setSection(serialize(cell.getValue()));
    return builder.build();
  }

  private static SerialSection serialize(Section section) {
    SerialSection.Builder builder = SerialSection.newBuilder();
    builder.setCourseId(section.getCourse().getId());
    builder.setSectionId(section.getSection());
    return builder.build();
  }

  public static Schedule deserialize(SerialSchedule schedule, Program program) {
    ImmutableTable.Builder<TimeBlock, Room, Section> builder = ImmutableTable.builder();
    for (SerialScheduleAssignment assignment : schedule.getAssignmentsList()) {
      builder.put(deserialize(assignment, program));
    }
    return new ImmutableSchedule(program, builder.build());
  }

  private static Section deserialize(SerialSection section, Program program) {
    return program.getCourse(section.getCourseId()).getSections().get(section.getSectionId());
  }

  private static Table.Cell<TimeBlock, Room, Section> deserialize(
      SerialScheduleAssignment assignment, Program program) {
    return Tables.immutableCell(
        program.getTimeBlock(assignment.getTimeBlockId()),
        program.getRoom(assignment.getRoomId()),
        deserialize(assignment.getSection(), program));
  }
}
