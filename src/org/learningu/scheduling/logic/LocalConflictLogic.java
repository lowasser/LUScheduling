package org.learningu.scheduling.logic;

import java.util.logging.Logger;

import org.learningu.scheduling.annotations.Flag;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Logic for verifying that a schedule assignment is internally consistent: the course can be
 * assigned to the room, the teachers are available during the period, etc.
 * 
 * @author lowasser
 */
public final class LocalConflictLogic extends ScheduleLogic {
  @Flag(
      value = "maxClassCapRatio",
      defaultValue = "4.0",
      description = "The maximum ratio of room capacity: class capacity.  "
          + "For example, if this was 1.25, the class would be forced to use a room at most 25% bigger than its cap. "
          + "The default value is 2.0.")
  private final double maxClassCapRatio;

  @Flag(
      value = "minClassCapRatio",
      defaultValue = "1.0",
      description = "The min ratio of room capacity: class capacity.  " 
          + "For example, if this was 0.5, the class would be forced to use a room at most 2 times bigger than its expected size. "
          + "The default value is 1.0.")
  private final double minClassCapRatio;

  @Inject
  LocalConflictLogic(@Named("maxClassCapRatio") double maxClassCapRatio,
      @Named("minClassCapRatio") double minClassCapRatio, Logger logger) {
    this.maxClassCapRatio = maxClassCapRatio;
    this.minClassCapRatio = minClassCapRatio;
    maxRatioConditionText = "Class cap : room capacity ratio must be <= " + maxClassCapRatio;
    minRatioConditionText = "Class cap : room capacity ratio must be >= "
        + minClassCapRatio;
    if (minClassCapRatio < 1.0) {
      logger.warning("Min class cap ratio is " + minClassCapRatio
          + ", which could result in classes being scheduled in rooms "
          + "smaller than the class size.");
    }
  }

  transient final String maxRatioConditionText;

  transient final String minRatioConditionText;

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    super.validate(validator, schedule, assignment);
    Section course = assignment.getSection();
    Room room = assignment.getRoom();
    double classSizeRatio = ((double) room.getCapacity()) / course.getMaxClassSize();
    validator.validateLocal(
        classSizeRatio <= maxClassCapRatio,
        assignment,
        maxRatioConditionText);
    validator.validateLocal(
        classSizeRatio >= minClassCapRatio,
        assignment,
        minRatioConditionText);
  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
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
