package org.learningu.scheduling.logic;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;
import org.learningu.scheduling.schedule.SerialSchedules.SerialStartAssignment;
import org.learningu.scheduling.schedule.StartAssignment;

public class LockedScheduleLogic extends ScheduleLogic {

  private final SerialSchedule fixed;
  
  @Inject
  LockedScheduleLogic(SerialSchedule schedule) {
    this.fixed = schedule;
  }
  
  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    Program program = schedule.getProgram();
    for (SerialStartAssignment assign : fixed.getAssignmentList()) {
      ClassPeriod period = program.getPeriod(assign.getPeriodId());
      Room room = program.getRoom(assign.getRoomId());
      Section section = program.getSection(assign.getSectionId());
      StartAssignment realAssign = schedule.getAssignmentsBySection().get(section);
      checkState(realAssign != null);
      checkState(realAssign.getPeriod().equals(period));
      checkState(realAssign.getRoom().equals(room));
    }
  }
}
