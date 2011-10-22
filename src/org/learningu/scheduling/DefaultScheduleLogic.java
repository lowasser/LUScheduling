package org.learningu.scheduling;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
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
final class DefaultScheduleLogic extends ScheduleLogic {

  private ScheduleLogicFlags flags;

  @Inject
  DefaultScheduleLogic(ScheduleLogicFlags flags) {
    this.flags = flags;
  }

  @Override
  public Condition isValid(Schedule schedule) {
    Condition valid = super.isValid(schedule);
    verifyNoTeacherConflicts(valid, schedule);
    verifyNoDuplicateCourses(valid, schedule);
    return valid;
  }

  @Override
  public boolean isCompatible(Course course, TimeBlock block) {
    return super.isCompatible(course, block)
        && course.getProgram().compatibleTimeBlocks(course).contains(block);
  }

  @Override
  public boolean isCompatible(Course course, Room room) {
    double estClassSizeRatio = ((double) course.getEstimatedClassSize()) / room.getCapacity();
    double classCapRatio = ((double) course.getMaxClassSize()) / room.getCapacity();
    return super.isCompatible(course, room)
        && estClassSizeRatio >= flags.minEstimatedClassSizeRatio
        && estClassSizeRatio <= flags.maxEstimatedClassSizeRatio
        && classCapRatio <= flags.maxClassCapRatio;
  }

  @Override
  public boolean isCompatible(Room room, TimeBlock block) {
    return super.isCompatible(room, block)
        && room.getProgram().compatibleTimeBlocks(room).contains(block);
  }

  @Override
  public boolean isCompatible(Teacher teacher, TimeBlock block) {
    return super.isCompatible(teacher, block)
        && teacher.getProgram().compatibleTimeBlocks(teacher).contains(block);
  }

  private Condition verifyNoTeacherConflicts(Condition parent, Schedule schedule) {
    Program program = schedule.getProgram();
    Table<TimeBlock, Room, Course> table = schedule.getScheduleTable();
    Condition teacherConflicts = parent.createSubCondition("teacherconflicts");
    teacherConflicts.getLogger().log(
        Level.FINEST,
        "Checking for teacher conflicts in schedule %s",
        schedule);

    for (Entry<TimeBlock, Map<Room, Course>> scheduleEntry : table.rowMap().entrySet()) {
      TimeBlock block = scheduleEntry.getKey();
      Map<Room, Course> roomAssignments = scheduleEntry.getValue();

      SetMultimap<Teacher, Course> coursesTeachingNow = HashMultimap.create(
          roomAssignments.size(),
          4);

      for (Course c : roomAssignments.values()) {
        for (Teacher t : program.teachersForCourse(c)) {
          coursesTeachingNow.put(t, c);
        }
      }

      for (Entry<Teacher, Collection<Course>> teacherAssignments : coursesTeachingNow.asMap()
          .entrySet()) {
        Teacher t = teacherAssignments.getKey();
        Collection<Course> teaching = teacherAssignments.getValue();
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

  private Condition verifyNoDuplicateCourses(Condition parent, Schedule schedule) {
    Table<TimeBlock, Room, Course> table = schedule.getScheduleTable();
    Map<Course, Table.Cell<TimeBlock, Room, Course>> cellMap = Maps.newHashMapWithExpectedSize(table.size());

    Condition duplicateCourses = parent.createSubCondition("duplicatecourses");

    duplicateCourses.getLogger().log(
        Level.FINEST,
        "Checking for duplicate courses in schedule %s",
        schedule);

    for (Cell<TimeBlock, Room, Course> cell : table.cellSet()) {
      Course c = cell.getValue();
      Cell<TimeBlock, Room, Course> previous = cellMap.get(c);
      duplicateCourses.verify(
          previous == null,
          "Course %s is scheduled twice: %s and %s",
          c,
          cell,
          previous);
      cellMap.put(c, cell);
    }

    return duplicateCourses;
  }
}
