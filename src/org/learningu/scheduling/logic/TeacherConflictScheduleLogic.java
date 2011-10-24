package org.learningu.scheduling.logic;

import java.util.Map.Entry;
import java.util.Set;

import org.learningu.scheduling.PresentAssignment;
import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Teacher;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Logic for verifying that an assignment would not require that teachers teach two classes at
 * once.
 * 
 * @author lowasser
 * 
 */
public class TeacherConflictScheduleLogic extends ScheduleLogic {

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
    super.validate(validator, schedule, assignment);
    Program program = schedule.getProgram();
    /*
     * Collecting the set of all courses taught by the same teachers is more efficient than going
     * through every teacher who is teaching a class this period.
     */
    Set<Teacher> teachers = program.teachersForCourse(assignment.getCourse());
    Set<Course> coursesTaughtBySame = coursesTaughtByTeachers(program, teachers);
    Set<PresentAssignment> conflicts = Sets.newLinkedHashSet();
    for (Entry<Room, PresentAssignment> entry : schedule
        .occurringAt(assignment.getPeriod())
        .entrySet()) {
      PresentAssignment assign = entry.getValue();
      Course course = assign.getCourse();
      if (coursesTaughtBySame.contains(course)) {
        conflicts.add(assign);
      }
    }

    validator.validateGlobal(
        assignment,
        conflicts,
        "Teachers must not be assigned to teach more than one class at a time");
  }

  static Set<Course> coursesTaughtByTeachers(Program program, Iterable<Teacher> teachers) {
    ImmutableSet.Builder<Course> builder = ImmutableSet.builder();
    for (Teacher t : teachers) {
      builder.addAll(program.getCoursesForTeacher(t));
    }
    return builder.build();
  }
}
