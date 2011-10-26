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
      value = "minClassCapRatio",
      defaultValue = "1.0",
      description = "The minimum ratio of the maximum capacity of a class to the capacity of the room for which it is scheduled. "
          + "For example, if this was 1.25, the class would be forced to use a room at least 25% bigger than its cap. "
          + "The default value is 1.0.")
  private final double maxClassCapRatio;

  @Flag(
      value = "maxEstClassSizeRatio",
      defaultValue = "1.0",
      description = "The maximum ratio of the estimated size of a class to the capacity of the room for which it is scheduled. "
          + "For example, if this was 0.5, the class would be forced to use a room at most 2 times bigger than its expected size. "
          + "The default value is 1.0.")
  private final double maxEstClassSizeRatio;

  @Inject
  LocalConflictLogic(@Named("minClassCapRatio") double maxClassCapRatio,
      @Named("maxEstClassSizeRatio") double maxEstClassSizeRatio, Logger logger) {
    this.maxClassCapRatio = maxClassCapRatio;
    this.maxEstClassSizeRatio = maxEstClassSizeRatio;
    classCapRatioConditionText = "Class cap : room capacity ratio must be <= " + maxClassCapRatio;
    estSizeRatioConditionText = "Est class size : room capacity ratio must be <= "
        + maxEstClassSizeRatio;
    if (maxClassCapRatio < 1.0) {
      logger.warning("Max class cap ratio is " + maxClassCapRatio
          + ", which could result in classes being scheduled in rooms "
          + "smaller than the class size.");
    }
  }

  transient final String classCapRatioConditionText;

  transient final String estSizeRatioConditionText;

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    super.validate(validator, schedule, assignment);
    Section course = assignment.getSection();
    Room room = assignment.getRoom();
    double estClassSizeRatio = ((double) course.getEstimatedClassSize()) / room.getCapacity();
    validator.validateLocal(
        estClassSizeRatio <= maxEstClassSizeRatio,
        assignment,
        estSizeRatioConditionText);
    double classCapRatio = ((double) course.getMaxClassSize()) / room.getCapacity();
    validator.validateLocal(
        classCapRatio <= maxClassCapRatio,
        assignment,
        classCapRatioConditionText);
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
