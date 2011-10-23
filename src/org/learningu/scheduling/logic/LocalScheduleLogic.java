package org.learningu.scheduling.logic;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.util.Flag;

import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
final class LocalScheduleLogic extends ScheduleLogic {

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

  @Inject
  LocalScheduleLogic(
      @Named("minEstimatedClassSizeRatio") double minEstimatedClassSizeRatio,
      @Named("maxEstimatedClassSizeRatio") double maxEstimatedClassSizeRatio,
      @Named("maxClassCapRatio") double maxClassCapRatio,
      @Named("localScheduleCheck") boolean localScheduleCheck) {
    this.minEstimatedClassSizeRatio = minEstimatedClassSizeRatio;
    this.maxEstimatedClassSizeRatio = maxEstimatedClassSizeRatio;
    this.maxClassCapRatio = maxClassCapRatio;
    this.localScheduleCheck = localScheduleCheck;
  }

  @Override
  public void validateOccurringAt(
      ScheduleValidator validator,
      Schedule schedule,
      ClassPeriod period,
      Room room,
      Section section) {
    if (localScheduleCheck) {
      Cell<ClassPeriod, Room, Section> assignment = Tables.immutableCell(period, room, section);

      Course course = section.getCourse();
      Program program = schedule.getProgram();
      validator.localValidate(
          program.compatiblePeriods(course).contains(period),
          assignment,
          "all teachers for course must be available during the specified period");

      double estClassSizeRatio = ((double) course.getEstimatedClassSize()) / room.getCapacity();
      double classCapRatio = ((double) course.getMaxClassSize()) / room.getCapacity();
      validator.localValidate(
          estClassSizeRatio >= minEstimatedClassSizeRatio
              && estClassSizeRatio <= maxEstimatedClassSizeRatio,
          assignment,
          String.format(
              "Estimated class size : room capacity ratio was %s but should have been between %s and %s",
              estClassSizeRatio,
              minEstimatedClassSizeRatio,
              maxEstimatedClassSizeRatio));
      validator.localValidate(classCapRatio <= maxClassCapRatio, assignment, String.format(
          "Class capacity : room capacity ratio was %s but should have been at most %s",
          classCapRatio,
          maxClassCapRatio));
      validator.localValidate(
          program.compatiblePeriods(room).contains(period),
          assignment,
          "rooms must be available during the specified period");
    }
  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule) {
    if (localScheduleCheck) {
      super.validate(validator, schedule);
    }
  }
}
