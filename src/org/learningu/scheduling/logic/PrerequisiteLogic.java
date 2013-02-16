package org.learningu.scheduling.logic;

import java.util.List;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

public final class PrerequisiteLogic extends ScheduleLogic {

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    super.validate(validator, schedule, assignment);
    // We mark this as a local conflict, since it can't be solved with deletions.
    Program program = schedule.getProgram();
    List<Course> prereqs = program.getPrerequisites(assignment.getSection());
    for (Course prereq : prereqs) {
      boolean valid = false;
      for (Section s : program.getSectionsOfCourse(prereq)) {
        StartAssignment sAssign = schedule.getAssignmentsBySection().get(s);
        if (sAssign == null) {
          continue;
        }
        List<PresentAssignment> sPAssigns = sAssign.getPresentAssignments();
        PresentAssignment lastAssign = sPAssigns.get(sPAssigns.size() - 1);
        if (lastAssign.getPeriod().compareTo(assignment.getPeriod()) < 0) {
          valid = true;
          break;
        }
      }
      validator.validateLocal(valid, assignment, "Section may not start before its prerequisites");
    }
  }

}
