package org.learningu.scheduling.logic;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.learningu.scheduling.PresentAssignment;
import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.StartAssignment;

/**
 * Logic for verifying that an assignment can be made to a schedule without any conflicts or
 * problems.
 * 
 * @author lowasser
 */
public class ScheduleLogic {
  public void validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    checkNotNull(validator);
    checkNotNull(schedule);
    checkNotNull(assignment);
    checkArgument(
        !schedule.startingAt(assignment.getRoom(), assignment.getPeriod()).isPresent(),
        "Cannot validate an assignment already in the schedule");
  }

  protected final void validatePresentAssignments(ScheduleValidator validator, Schedule schedule,
      StartAssignment assignment) {
    for (PresentAssignment present : assignment.getPresentAssignments()) {
      validate(validator, schedule, present);
    }
  }

  public void validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
    checkNotNull(validator);
    checkNotNull(schedule);
    checkNotNull(assignment);
    checkArgument(
        !schedule.occurringAt(assignment.getPeriod(), assignment.getRoom()).isPresent(),
        "Cannot validate an assignment already in the schedule");
  }
}
