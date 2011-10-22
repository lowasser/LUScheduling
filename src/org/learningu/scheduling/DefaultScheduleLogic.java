package org.learningu.scheduling;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;
import org.learningu.scheduling.util.Condition;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.inject.Inject;

/**
 * A default implementation of {@code ScheduleLogic}.
 * 
 * @author lowasser
 */
final class DefaultScheduleLogic implements ScheduleLogic {

  private ScheduleLogicFlags flags;
  private final Logger logger;

  @Inject
  DefaultScheduleLogic(ScheduleLogicFlags flags, Logger logger) {
    this.flags = flags;
    this.logger = logger;
  }

  public Logger getLogger() {
    return logger;
  }

  @Override
  public Condition isValid(Schedule schedule) {
    Condition valid = Condition.create(getLogger(), Level.FINE);
    if (flags.localScheduleCheck) {
      verifyLocallyValid(valid, schedule);
    }
    if (flags.teacherConflictCheck) {
      verifyNoTeacherConflicts(valid, schedule);
    }
    if (flags.doublyScheduledSectionsCheck) {
      verifyNoDuplicateSections(valid, schedule);
    }
    return valid;
  }

  protected Condition verifyLocallyValid(Condition parent, Schedule schedule) {
    Table<ClassPeriod, Room, Section> table = schedule.getScheduleTable();
    Condition local = parent.createSubCondition("local");
    local.log(Level.FINEST, "Testing local validity of schedule");
    for (Cell<ClassPeriod, Room, Section> cell : table.cellSet()) {
      verifyLocallyValid(local, cell);
    }
    local.log(Level.FINEST, "Done testing local validity of schedule");
    return local;
  }

  protected Condition verifyLocallyValid(Condition local, Cell<ClassPeriod, Room, Section> cell) {
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

  protected void verifyCompatible(Condition cond, Course course, ClassPeriod period) {
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

  protected void verifyCompatible(Condition cond, Course course, Room room) {
    double estClassSizeRatio = ((double) course.getEstimatedClassSize()) / room.getCapacity();
    double classCapRatio = ((double) course.getMaxClassSize()) / room.getCapacity();
    cond.verify(
        course.getProgram() == room.getProgram(),
        "Program mismatch between %s and %s",
        course,
        room);
    cond
        .verify(
            estClassSizeRatio >= flags.minEstimatedClassSizeRatio
                && estClassSizeRatio <= flags.maxEstimatedClassSizeRatio,
            "Estimated class size : room capacity ratio was %s but should have been between %s and %s",
            estClassSizeRatio,
            flags.minEstimatedClassSizeRatio,
            flags.maxEstimatedClassSizeRatio);
    cond.verify(
        classCapRatio <= flags.maxClassCapRatio,
        "Class capacity : room capacity ratio was %s but should have been at most %s",
        classCapRatio,
        flags.maxClassCapRatio);
  }

  protected void verifyCompatible(Condition cond, Room room, ClassPeriod period) {
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

  private Condition verifyNoTeacherConflicts(Condition parent, Schedule schedule) {
    Program program = schedule.getProgram();
    Table<ClassPeriod, Room, Section> table = schedule.getScheduleTable();
    Condition teacherConflicts = parent.createSubCondition("teacherconflicts");
    teacherConflicts.getLogger().log(
        Level.FINEST,
        "Checking for teacher conflicts in schedule %s",
        schedule);

    for (Entry<ClassPeriod, Map<Room, Section>> scheduleEntry : table.rowMap().entrySet()) {
      ClassPeriod block = scheduleEntry.getKey();
      Map<Room, Section> roomAssignments = scheduleEntry.getValue();

      SetMultimap<Teacher, Section> coursesTeachingNow = HashMultimap.create(
          roomAssignments.size(),
          4);

      for (Section s : roomAssignments.values()) {
        for (Teacher t : program.teachersForCourse(s.getCourse())) {
          coursesTeachingNow.put(t, s);
        }
      }

      for (Entry<Teacher, Collection<Section>> teacherAssignments : coursesTeachingNow
          .asMap()
          .entrySet()) {
        Teacher t = teacherAssignments.getKey();
        Collection<Section> teaching = teacherAssignments.getValue();
        teacherConflicts.verify(
            teaching.size() <= 1,
            "Teacher %s has conflicts between %s classes in block %s: %s",
            t,
            teaching.size(),
            block,
            teaching);
      }
    }

    return teacherConflicts;
  }

  private Condition verifyNoDuplicateSections(Condition parent, Schedule schedule) {
    Table<ClassPeriod, Room, Section> table = schedule.getScheduleTable();
    Map<Section, Table.Cell<ClassPeriod, Room, Section>> cellMap = Maps
        .newHashMapWithExpectedSize(table.size());

    Condition duplicateCourses = parent.createSubCondition("duplicatecourses");

    duplicateCourses.getLogger().log(
        Level.FINEST,
        "Checking for duplicate courses in schedule %s",
        schedule);

    for (Cell<ClassPeriod, Room, Section> cell : table.cellSet()) {
      Section s = cell.getValue();
      Cell<ClassPeriod, Room, Section> previous = cellMap.get(s);
      duplicateCourses.verify(
          previous == null,
          "Section %s is scheduled twice: %s and %s",
          s,
          cell,
          previous);
      cellMap.put(s, cell);
    }

    return duplicateCourses;
  }
}
