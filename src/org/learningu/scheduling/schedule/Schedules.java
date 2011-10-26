package org.learningu.scheduling.schedule;

import static com.google.common.base.Preconditions.checkState;

import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.logic.ScheduleValidator;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;
import org.learningu.scheduling.schedule.SerialSchedules.SerialStartAssignment;
import org.learningu.scheduling.util.ModifiedState;

public final class Schedules {
  private Schedules() {
  }

  public static final Schedule deserialize(Schedule.Factory factory, SerialSchedule serial) {
    Schedule current = factory.create();
    Program program = current.getProgram();

    for (SerialStartAssignment serialAssign : serial.getAssignmentList()) {
      StartAssignment assign =
          StartAssignment.create(
              program.getPeriod(serialAssign.getPeriodId()),
              program.getRoom(serialAssign.getRoomId()),
              program.getSection(serialAssign.getSectionId()));
      ModifiedState<ScheduleValidator, Schedule> modified = current.assignStart(assign);
      checkState(
          modified.getResult().isValid(),
          "Schedule conflict when adding assignment: %s",
          modified.getResult());
      current = modified.getNewState();
    }

    return current;
  }

  public static final SerialSchedule serialize(Schedule schedule) {
    SerialSchedule.Builder scheduleBuilder = SerialSchedule.newBuilder();
    for (StartAssignment assign : schedule.startAssignments()) {
      scheduleBuilder.addAssignment(SerialStartAssignment.newBuilder()
          .setPeriodId(assign.getPeriod().getId())
          .setRoomId(assign.getRoom().getId())
          .setSectionId(assign.getSection().getId()));
    }
    return scheduleBuilder.build();
  }
}
