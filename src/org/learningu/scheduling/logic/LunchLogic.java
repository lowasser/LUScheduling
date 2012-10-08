package org.learningu.scheduling.logic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

public class LunchLogic extends ScheduleLogic {

  @Override
  public
      void
      validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    // Check for this time block and the teachers for this class
    Program program = schedule.getProgram();
    TimeBlock block = assignment.getTimeBlock();
    
    List<ClassPeriod> lunches = Lists.newArrayList();
    for (ClassPeriod period : block.getPeriods()) {
      if (period.isLunch()) {
        lunches.add(period);
      }
    }
    
    // If there are no lunch periods intersecting with this assignment, skip the test.
    if (!lunches.removeAll(assignment.getPresentPeriods())) {
      return;
    }
    
    for (Teacher t : assignment.getCourse().getTeachers()) {
      Set<ClassPeriod> myLunches = Sets.newHashSet(lunches);
      List<StartAssignment> conflicts = Lists.newArrayList();
      Map<Section, StartAssignment> sched = schedule.getAssignmentsBySection();
      for (Section s : program.getSectionsForTeacher(t)) {
        StartAssignment assign = sched.get(s);
        if (assign != null && assign.getTimeBlock().equals(block)) {
          if (myLunches.removeAll(assign.getPresentPeriods())) {
            conflicts.add(assign);
          }
        }
      }
      validator.validateGlobal(assignment, conflicts,
          "Teacher " + t + " must have at least one lunch period in " + block);
    }
  }
}
