package org.learningu.scheduling;

import org.learningu.scheduling.util.Flag;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * An injectable collection of flags for scheduling logic.
 * 
 * @author lowasser
 */
public final class ScheduleLogicFlags {
  public static final ScheduleLogicFlags DEFAULTS = new ScheduleLogicFlags(0.0, 1.0, 1.0, true,
      true, true);

  @Flag(
      value = "minEstimatedClassSizeRatio",
      description = "The smallest estimated class size : room capacity ratio that is acceptable.  "
          + "For example, if this was 0.5, a class estimated to have 15 students would not be "
          + "allowed into a room with capacity over 30. Default is 0.",
      defaultValue = "0.0")
  final double minEstimatedClassSizeRatio;

  @Flag(
      value = "maxEstimatedClassSizeRatio",
      description = "The greatest estimated class size : room capacity ratio that is acceptable.  "
          + "For example, if this was 0.5, a class estimated to have 15 students would not be "
          + "allowed into a room with capacity under 30.  Default is 1.",
      defaultValue = "1.0")
  final double maxEstimatedClassSizeRatio;

  @Flag(value = "maxClassCapRatio", description = "The greatest class class cap : "
      + "room capacity ratio that is acceptable.  For example, if this was 0.5, a class with a "
      + " cap of 15 students would not be allowed into a room with capacity under 30."
      + " Default is 1.", defaultValue = "1.0")
  final double maxClassCapRatio;

  @Flag(
      value = "localScheduleCheck",
      description = "Check that schedule assignments are locally valid: that teachers are "
          + "available at the appropriate times, that rooms are large enough for courses, "
          + "and other checks which only affect a single course/room/time assignment.",
      defaultValue = "true")
  final boolean localScheduleCheck;

  @Flag(
      value = "teacherConflictCheck",
      description = "Check for teachers scheduled to teach more than one class in the same block.",
      defaultValue = "true")
  final boolean teacherConflictCheck;

  @Flag(
      value = "doublyScheduledSectionsCheck",
      description = "Check for sections appearing more than once in the schedule.",
      defaultValue = "true")
  final boolean doublyScheduledSectionsCheck;

  @Inject
  ScheduleLogicFlags(
      @Named("minEstimatedClassSizeRatio") double minEstimatedClassSizeRatio,
      @Named("maxEstimatedClassSizeRatio") double maxEstimatedClassSizeRatio,
      @Named("maxClassCapRatio") double maxClassCapRatio,
      @Named("teacherConflictCheck") boolean teacherConflictCheck,
      @Named("doublyScheduledSectionsCheck") boolean doublyScheduledCoursesCheck,
      @Named("localScheduleCheck") boolean localScheduleCheck) {
    this.minEstimatedClassSizeRatio = minEstimatedClassSizeRatio;
    this.maxEstimatedClassSizeRatio = maxEstimatedClassSizeRatio;
    this.maxClassCapRatio = maxClassCapRatio;
    this.teacherConflictCheck = teacherConflictCheck;
    this.doublyScheduledSectionsCheck = doublyScheduledCoursesCheck;
    this.localScheduleCheck = localScheduleCheck;
  }
}