package org.learningu.scheduling.logic;

import com.google.common.base.Optional;

import java.util.Map;

import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

/**
 * Logic for checking that there aren't multiple instances of the same section.
 * 
 * @author lowasser
 */
public final class DuplicateSectionLogic extends ScheduleLogic {

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule,
      final StartAssignment assignment) {
    super.validate(validator, schedule, assignment);
    Map<Section, StartAssignment> assignmentsBySection = schedule.getAssignmentsBySection();
    StartAssignment sectionAssignment = assignmentsBySection.get(assignment.getSection());
    // sectionAssignment is null iff this section has not been scheduled

    validator.validateGlobal(
        assignment,
        Optional.fromNullable(sectionAssignment).asSet(),
        "sections must not already be scheduled in the schedule");
  }
}
