package org.learningu.scheduling.logic;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;

public final class GradeRangeLogic extends ScheduleLogic {
  @Override
  public
      void
      validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
    super.validate(validator, schedule, assignment);
    ClassPeriod period = assignment.getPeriod();
    Course course = assignment.getCourse();
    validator.validateLocal(
        period.getGradeRange().encloses(course.getGradeRange()),
        assignment,
        "Classes must be scheduled during periods that allow each of the appropriate grades");
  }
}
