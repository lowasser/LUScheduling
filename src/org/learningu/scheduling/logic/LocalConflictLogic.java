package org.learningu.scheduling.logic;

import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

import edu.uchicago.lowasser.flaginjection.Flag;

/**
 * Logic for verifying that a schedule assignment is internally consistent: the course can be
 * assigned to the room, the teachers are available during the period, etc.
 * 
 * @author lowasser
 */
public final class LocalConflictLogic extends ScheduleLogic {
  @Flag(
      name = "maxClassCapRatio",
      description = "The maximum ratio of room capacity: class capacity.  "
          + "For example, if this was 1.25, the class would be forced to use a room at most 25% bigger than its cap. "
          + "The default value is 2.0.",
      optional = true)
  private double maxClassCapRatio = 1.5;

  @Flag(
      name = "minClassCapRatio",
      description = "The min ratio of room capacity: class capacity.  "
          + "For example, if this was 1.25, the class would be forced to use a room at least 25% bigger than its cap. "
          + "The default value is 0.9.",
      optional = true)
  private double minClassCapRatio = 0.9;

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    super.validate(validator, schedule, assignment);
    Section course = assignment.getSection();
    Room room = assignment.getRoom();
    double classSizeRatio = ((double) room.getCapacity()) / course.getMaxClassSize();
    validator.validateLocal(
        classSizeRatio <= maxClassCapRatio,
        assignment,
        "Class cap:room capacity ratio is too high");
    validator.validateLocal(
        classSizeRatio >= minClassCapRatio,
        assignment,
        "Class cap:room capacity ratio is too low");
  }

  @Override
  public
      void
      validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
    super.validate(validator, schedule, assignment);
    Program program = schedule.getProgram();
    validator.validateLocal(
        program.compatiblePeriods(assignment.getSection()).contains(assignment.getPeriod()),
        assignment,
        "All teachers for a course must be available during all periods in which it is scheduled");
    validator.validateLocal(
        program.compatiblePeriods(assignment.getRoom()).contains(assignment.getPeriod()),
        assignment,
        "Courses cannot be scheduled to rooms while the room is unavailable");
  }
}
