package org.learningu.scheduling;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;
import org.learningu.scheduling.util.Condition;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

public final class Schedules {
  private Schedules() {
  }

  public static Set<Course> unscheduledCourses(Schedule schedule) {
    Set<Course> courses = Sets.newLinkedHashSet(schedule.getProgram().getCourses());
    courses.removeAll(schedule.getScheduleTable().values());
    return ImmutableSet.copyOf(courses);
  }

  public static Condition checkScheduleIsValid(Schedule schedule, Logger logger) {
    Condition cond = Condition.create(logger, Level.WARNING);
    verifyLocallyValid(cond, schedule);
    verifyNoTeacherConflicts(cond, schedule);
    verifyCoursesScheduledOnce(cond, schedule);
    return cond;
  }

  private static void verifyLocallyValid(Condition parent, Schedule schedule) {
    Condition locallyValid = parent.createSubCondition("local");
    ScheduleLogic logic = schedule.getLogic();
    for (Table.Cell<TimeBlock, Room, Course> cell : schedule.getScheduleTable().cellSet()) {
      verifyLocallyValid(locallyValid, logic, cell);
    }
  }

  private static void verifyLocallyValid(Condition validSchedule,
      ScheduleLogic logic,
      Table.Cell<TimeBlock, Room, Course> cell) {
    TimeBlock block = cell.getRowKey();
    Room room = cell.getColumnKey();
    Course course = cell.getValue();
    validSchedule.verify(
        logic.isCompatible(course, room),
        "Course %s cannot occur in room %s",
        course,
        room);
    validSchedule.verify(
        logic.isCompatible(course, block),
        "Course %s cannot occur in block %s",
        course,
        block);
    validSchedule.verify(
        logic.isCompatible(room, block),
        "Room %s is not available in block %s",
        room,
        block);
  }

  private static void verifyNoTeacherConflicts(Condition parent, Schedule schedule) {
    Condition noTeacherConflicts = parent.createSubCondition("teacher_conflicts");

    Program program = schedule.getProgram();
    Table<TimeBlock, Room, Course> table = schedule.getScheduleTable();
    for (Map.Entry<TimeBlock, Map<Room, Course>> scheduleForBlock : table.rowMap().entrySet()) {
      TimeBlock block = scheduleForBlock.getKey();
      Map<Room, Course> roomAssignmentsForBlock = scheduleForBlock.getValue();
      SetMultimap<Teacher, Course> teacherSchedules = HashMultimap.create(
          roomAssignmentsForBlock.size(),
          2);

      for (Course c : roomAssignmentsForBlock.values()) {
        for (Teacher t : program.teachersForCourse(c)) {
          teacherSchedules.put(t, c);
        }
      }

      // Check that no teacher occurs more than once!
      for (Map.Entry<Teacher, Collection<Course>> entry : teacherSchedules.asMap().entrySet()) {
        Teacher t = entry.getKey();
        Collection<Course> courses = entry.getValue();
        noTeacherConflicts.verify(
            courses.size() <= 1,
            "Teacher %s is scheduled to teach %s classes during block %s: %s",
            t,
            courses.size(),
            block,
            courses);
      }
    }
  }

  private static void verifyCoursesScheduledOnce(Condition parent, Schedule schedule) {
    Condition noDuplicates = parent.createSubCondition("courses_scheduled_once");
    Table<TimeBlock, Room, Course> table = schedule.getScheduleTable();
    Map<Course, Table.Cell<TimeBlock, Room, Course>> cellMap = Maps.newHashMapWithExpectedSize(table.size());
    for (Table.Cell<TimeBlock, Room, Course> cell : table.cellSet()) {
      Course c = cell.getValue();
      Cell<TimeBlock, Room, Course> previous = cellMap.get(c);
      noDuplicates.verify(
          previous == null,
          "Course %s has the following duplicate schedulings: %s, %s",
          c,
          cell,
          previous);
      cellMap.put(c, cell);
    }
  }
}
