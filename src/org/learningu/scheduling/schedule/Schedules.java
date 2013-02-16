package org.learningu.scheduling.schedule;

import static com.google.common.base.Preconditions.checkState;

import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.logic.ScheduleValidator;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;
import org.learningu.scheduling.schedule.SerialSchedules.SerialStartAssignment;
import org.learningu.scheduling.util.ModifiedState;

/**
 * Utilities for converting between the protobuf-based {@link SerialSchedule} objects and the
 * {@code Schedule} objects for use at runtime.
 * 
 * @author lowasser
 */
public final class Schedules {
  private Schedules() {
  }

  public static final Schedule deserialize(Schedule.Factory factory, SerialSchedule serial) {
    Schedule current = factory.create();
    Program program = current.getProgram();

    boolean good = true;
    for (SerialStartAssignment serialAssign : serial.getAssignmentList()) {
      try {
      StartAssignment assign = StartAssignment.create(
          program.getPeriod(serialAssign.getPeriodId()),
          program.getRoom(serialAssign.getRoomId()),
          program.getSection(serialAssign.getSectionId()),
          serialAssign.getLocked());
      ModifiedState<ScheduleValidator, Schedule> modified = current.assignStart(assign);
      if (!(modified.getResult().isValid() && modified.getNewState() != current)) {
        System.out.printf(
            "Schedule conflict when adding assignment: %s%n",
            modified.getResult());
        good = false;
      }
      current = modified.getNewState();
      } catch (IllegalStateException | IllegalArgumentException e) {
        System.out.printf("Ignoring %s, continuing%n", e.getMessage());
      }
    }

    if (!good) {
      throw new AssertionError();
    }
    
    return current;
  }

  public static final SerialSchedule serialize(Schedule schedule) {
    SerialSchedule.Builder scheduleBuilder = SerialSchedule.newBuilder();
    for (StartAssignment assign : schedule.getStartAssignments()) {
      scheduleBuilder.addAssignment(SerialStartAssignment
          .newBuilder()
          .setPeriodId(assign.getPeriod().getId())
          .setRoomId(assign.getRoom().getId())
          .setSectionId(assign.getSection().getId()));
    }
    return scheduleBuilder.build();
  }
}
