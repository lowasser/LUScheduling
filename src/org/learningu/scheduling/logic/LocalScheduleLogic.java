package org.learningu.scheduling.logic;

import java.util.Set;
import java.util.logging.Level;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.util.Condition;
import org.learningu.scheduling.util.Flag;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
final class LocalScheduleLogic implements ScheduleLogic {

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
  public Condition isValid(Condition local, Schedule schedule) {
    Table<ClassPeriod, Room, Section> table = schedule.getScheduleTable();
    if (localScheduleCheck) {
      local.log(Level.FINEST, "Testing local validity of schedule");
      for (Cell<ClassPeriod, Room, Section> cell : table.cellSet()) {
        verifyLocallyValid(local, cell);
      }
      local.log(Level.FINEST, "Done testing local validity of schedule");
    }
    return local;
  }

  private Condition verifyLocallyValid(Condition local, Cell<ClassPeriod, Room, Section> cell) {
    ClassPeriod period = cell.getRowKey();
    Room room = cell.getColumnKey();
    Course course = cell.getValue().getCourse();
    local.log(Level.FINEST, "Testing local validity of %s", cell);
    verifyCompatible(local, course, period);
    verifyCompatible(local, room, period);
    verifyCompatible(local, course, room);
    local.log(Level.FINEST, "Done testing local validity of %s", cell);
    return local;
  }

  private void verifyCompatible(Condition cond, Course course, ClassPeriod period) {
    cond.verify(
        course.getProgram() == period.getProgram(),
        "Program mismatch between %s and %s",
        course,
        period);
    Set<ClassPeriod> compatibleBlocks = course.getProgram().compatiblePeriods(course);
    cond.verify(
        compatibleBlocks.contains(period),
        "Course %s is scheduled for %s but is only available during %s",
        course,
        period,
        compatibleBlocks);
  }

  private void verifyCompatible(Condition cond, Course course, Room room) {
    double estClassSizeRatio = ((double) course.getEstimatedClassSize()) / room.getCapacity();
    double classCapRatio = ((double) course.getMaxClassSize()) / room.getCapacity();
    cond.verify(
        course.getProgram() == room.getProgram(),
        "Program mismatch between %s and %s",
        course,
        room);
    cond.verify(
        estClassSizeRatio >= minEstimatedClassSizeRatio
            && estClassSizeRatio <= maxEstimatedClassSizeRatio,
        "Estimated class size : room capacity ratio was %s but should have been between %s and %s",
        estClassSizeRatio,
        minEstimatedClassSizeRatio,
        maxEstimatedClassSizeRatio);
    cond.verify(
        classCapRatio <= maxClassCapRatio,
        "Class capacity : room capacity ratio was %s but should have been at most %s",
        classCapRatio,
        maxClassCapRatio);
  }

  private void verifyCompatible(Condition cond, Room room, ClassPeriod period) {
    cond.verify(
        period.getProgram() == room.getProgram(),
        "Program mismatch between %s and %s",
        period,
        room);
    Set<ClassPeriod> compatibleBlocks = room.getProgram().compatiblePeriods(room);
    cond.verify(
        compatibleBlocks.contains(period),
        "Room %s is scheduled for a class in block %s but is only available during: %s",
        room,
        period,
        compatibleBlocks);
  }
}
