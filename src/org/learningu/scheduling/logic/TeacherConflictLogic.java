package org.learningu.scheduling.logic;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;

/**
 * Logic for verifying that an assignment would not require that teachers teach two classes at
 * once.
 * 
 * @author lowasser
 * 
 */
public final class TeacherConflictLogic extends ScheduleLogic {

  @Override
  public
      void
      validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
    super.validate(validator, schedule, assignment);
    Program program = schedule.getProgram();
    /*
     * Collecting the set of all courses taught by the same teachers is more efficient than going
     * through every teacher who is teaching a class this period.
     */
    List<Teacher> teachers = program.teachersFor(assignment.getSection());
    final Set<Section> coursesTaughtBySame = coursesTaughtByTeachers(program, teachers);
    Predicate<PresentAssignment> hasConflict = new Predicate<PresentAssignment>() {
      @Override
      public boolean apply(PresentAssignment input) {
        return coursesTaughtBySame.contains(input.getSection());
      }
    };
    List<PresentAssignment> conflicts = Lists.newArrayList(Iterables.filter(
        schedule.occurringAt(assignment.getPeriod()).values(),
        hasConflict));
    validator.validateGlobal(
        assignment,
        conflicts,
        "Teachers must not be assigned to teach more than one class at a time");
  }

  static Set<Section> coursesTaughtByTeachers(Program program, Iterable<Teacher> teachers) {
    ImmutableSet.Builder<Section> builder = ImmutableSet.builder();
    for (Teacher t : teachers) {
      builder.addAll(program.getSectionsForTeacher(t));
    }
    return builder.build();
  }
}
