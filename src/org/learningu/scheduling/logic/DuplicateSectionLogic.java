package org.learningu.scheduling.logic;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

/**
 * Logic for checking that there aren't multiple instances of the same section.
 * 
 * @author lowasser
 */
public final class DuplicateSectionLogic extends ScheduleLogic {

  @Override
  public void validate(
      ScheduleValidator validator,
      Schedule schedule,
      final StartAssignment assignment) {
    super.validate(validator, schedule, assignment);
    Iterable<StartAssignment> duplicates = Iterables.filter(
        schedule.getStartAssignments(),
        new Predicate<StartAssignment>() {
          @Override
          public boolean apply(StartAssignment input) {
            return input.getSection().equals(assignment.getSection());
          }
        });
    validator.validateGlobal(
        assignment,
        duplicates,
        "sections must not already be scheduled in the schedule");
  }

}
