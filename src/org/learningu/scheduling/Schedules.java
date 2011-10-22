package org.learningu.scheduling;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Serial.SerialPeriod;
import org.learningu.scheduling.graph.Serial.SerialSchedule;
import org.learningu.scheduling.graph.Serial.SerialSchedule.SerialScheduleAssignment;
import org.learningu.scheduling.graph.Serial.SerialSection;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;

public class Schedules {
  private Schedules() {
  }

  public static SerialSchedule serialize(Schedule schedule) {
    SerialSchedule.Builder builder = SerialSchedule.newBuilder();
    for (Cell<ClassPeriod, Room, Section> cell : schedule.getStartingTimeTable().cellSet()) {
      builder.addAssignments(serialize(cell));
    }
    return builder.build();
  }

  private static SerialScheduleAssignment serialize(Table.Cell<ClassPeriod, Room, Section> cell) {
    SerialScheduleAssignment.Builder builder = SerialScheduleAssignment.newBuilder();
    builder.setPeriod(serialize(cell.getRowKey()));
    builder.setRoomId(cell.getColumnKey().getId());
    builder.setSection(serialize(cell.getValue()));
    return builder.build();
  }

  private static SerialPeriod serialize(ClassPeriod period) {
    SerialPeriod.Builder builder = SerialPeriod.newBuilder();
    builder.setPeriodId(period.getId());
    builder.setDescription(period.getDescription());
    return builder.build();
  }

  private static SerialSection serialize(Section section) {
    SerialSection.Builder builder = SerialSection.newBuilder();
    builder.setCourseId(section.getCourse().getId());
    builder.setSectionId(section.getSection());
    return builder.build();
  }

  public static Schedule deserialize(SerialSchedule schedule, Program program) {
    ImmutableTable.Builder<ClassPeriod, Room, Section> builder = ImmutableTable.builder();
    for (SerialScheduleAssignment assignment : schedule.getAssignmentsList()) {
      builder.put(deserialize(assignment, program));
    }
    return ImmutableSchedule.create(program, builder.build());
  }

  private static Section deserialize(SerialSection section, Program program) {
    return program.getCourse(section.getCourseId()).getSections().get(section.getSectionId());
  }

  private static Table.Cell<ClassPeriod, Room, Section> deserialize(
      SerialScheduleAssignment assignment,
      Program program) {
    return Tables.immutableCell(
        deserialize(assignment.getPeriod(), program),
        program.getRoom(assignment.getRoomId()),
        deserialize(assignment.getSection(), program));
  }

  public static ClassPeriod deserialize(SerialPeriod period, Program program) {
    return program.getPeriod(period.getPeriodId());
  }
}
