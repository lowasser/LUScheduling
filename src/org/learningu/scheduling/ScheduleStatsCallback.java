package org.learningu.scheduling;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

public class ScheduleStatsCallback extends BasicFutureCallback<Schedule> {
  @Inject
  ScheduleStatsCallback(Logger logger) {
    super(logger);
  }

  @Override
  public void process(Schedule schedule) {
    sections(schedule);
    teachers(schedule);
    courses(schedule);
    rooms(schedule);
  }

  private void sections(Schedule schedule) {
    logger.log(Level.INFO, "{0}/{1} sections scheduled", new Object[] {
        schedule.getScheduledSections().size(), schedule.getProgram().getSections().size() });
  }

  private void teachers(Schedule schedule) {
    Program program = schedule.getProgram();
    int nTeachers = program.getTeachers().size();
    Set<Teacher> distinctTeachers = Sets.newHashSetWithExpectedSize(nTeachers);
    for (StartAssignment assignment : schedule.getStartAssignments()) {
      distinctTeachers.addAll(program.teachersFor(assignment.getCourse()));
    }
    logger.log(Level.INFO, "{0}/{1} teachers have at least one class scheduled", new Object[] {
        distinctTeachers.size(), nTeachers });
  }

  private void courses(Schedule schedule) {
    Set<Course> courses = ImmutableSet.copyOf(Collections2.transform(
        schedule.getStartAssignments(),
        new Function<StartAssignment, Course>() {
          @Override
          public Course apply(StartAssignment input) {
            return input.getCourse();
          }
        }));
    logger.log(Level.INFO, "{0}/{1} courses have at least one section scheduled", new Object[] {
        courses.size(), schedule.getProgram().getCourses().size() });
  }

  private void rooms(Schedule schedule) {
    int availRoomBlocks = 0;
    int usedRoomBlocks = 0;
    Program program = schedule.getProgram();
    for (Room r : program.getRooms()) {
      availRoomBlocks += program.compatiblePeriods(r).size();
    }
    for (StartAssignment assign : schedule.getStartAssignments()) {
      usedRoomBlocks += assign.getCourse().getPeriodLength();
    }
    logger.log(Level.INFO, "{0}/{1} room-time blocks scheduled", new Object[] { usedRoomBlocks,
        availRoomBlocks });
  }
}
