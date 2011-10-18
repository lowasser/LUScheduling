package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;

public final class Schedules {
  private Schedules() {
  }

  public static void checkScheduleIsValid(Schedule schedule) {
    checkScheduleMatchesWithProgram(schedule);
    checkCoursesScheduledOnce(schedule);
    checkNoTeacherConflicts(schedule);
    checkNoRoomConflicts(schedule);
  }

  private static void checkNoTeacherConflicts(Schedule schedule) {
    for (Map.Entry<TimeBlock, Map<Room, Course>> scheduleForBlock : schedule
        .getScheduleTable()
        .rowMap()
        .entrySet()) {
      TimeBlock block = scheduleForBlock.getKey();
      Multiset<Teacher> teacherSchedules = HashMultiset.create();
      for (Course scheduledCourse : scheduleForBlock.getValue().values()) {
        teacherSchedules.addAll(scheduledCourse.getTeachers());
      }

      // Check that no teacher occurs more than once!
      for (Multiset.Entry<Teacher> entry : teacherSchedules.entrySet()) {
        Teacher teacher = entry.getElement();
        checkArgument(
            entry.getCount() <= 1, "Teacher %s is scheduled to teach more than once in block %s",
            teacher, block);
        checkArgument(
            schedule.getScheduler().isCompatible(teacher, block),
            "Teacher %s cannot teach during block %s");
      }
    }
  }

  private static void checkNoRoomConflicts(Schedule schedule) {
    for (Table.Cell<TimeBlock, Room, Course> assignment : schedule.getScheduleTable().cellSet()) {
      Course course = assignment.getValue();
      Room room = assignment.getColumnKey();
      checkArgument(
          schedule.getScheduler().isCompatible(course, room),
          "Course %s and room %s are incompatible", course, room);
    }
  }

  private static void checkCoursesScheduledOnce(Schedule schedule) {
    Multiset<Course> uniqueScheduledCourses = ImmutableMultiset.copyOf(schedule
        .getScheduleTable()
        .values());
    checkArgument(
        uniqueScheduledCourses.size() == uniqueScheduledCourses.elementSet().size(),
        "Courses are scheduled more than once in schedule %s", schedule);
  }

  private static void checkScheduleMatchesWithProgram(Schedule schedule) {
    for (TimeBlock block : schedule.getScheduleTable().rowKeySet()) {
      checkArgument(
          block.getProgram() == schedule.getProgram(), "Block is not associated with this program");
    }
    for (Room room : schedule.getScheduleTable().columnKeySet()) {
      checkArgument(
          room.getProgram() == schedule.getProgram(), "Room is not associated with this program");
    }
    for (Course course : schedule.getScheduleTable().values()) {
      checkArgument(
          course.getProgram() == schedule.getProgram(),
          "Course is not associated with this program");
    }
  }
}
