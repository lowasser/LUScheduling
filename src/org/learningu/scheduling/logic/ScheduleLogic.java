package org.learningu.scheduling.logic;

import org.learningu.scheduling.PresentAssignment;
import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.StartAssignment;

public class ScheduleLogic {
  public void validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    for (PresentAssignment present : assignment.getPresentAssignments()) {
      validate(validator, schedule, present);
    }
  }

  public void validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
  }
}
