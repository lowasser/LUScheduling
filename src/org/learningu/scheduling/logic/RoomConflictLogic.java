package org.learningu.scheduling.logic;

import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;

/**
 * Logic for verifying that the assignment would not conflict with some other class.
 * 
 * @author lowasser
 */
public final class RoomConflictLogic extends ScheduleLogic {

  @Override
  public
      void
      validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
    super.validate(validator, schedule, assignment);
    validator.validateGlobal(
        assignment,
        schedule.occurringAt(assignment.getPeriod(), assignment.getRoom()).asSet(),
        "Classes may not use the same room at the same time");
  }
}
