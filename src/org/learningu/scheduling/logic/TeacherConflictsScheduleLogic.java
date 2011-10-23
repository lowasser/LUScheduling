package org.learningu.scheduling.logic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.util.Flag;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
final class TeacherConflictsScheduleLogic extends ScheduleLogic {

  @Flag(
      value = "teacherConflictCheck",
      description = "Check for teachers scheduled to teach more than one class in the same block.",
      defaultValue = "true")
  final boolean teacherConflictCheck;

  @Inject
  TeacherConflictsScheduleLogic(@Named("teacherConflictCheck") boolean teacherConflictCheck) {
    this.teacherConflictCheck = teacherConflictCheck;
  }

  @Override
  public void validateStartingAt(
      ScheduleValidator validator,
      Schedule schedule,
      ClassPeriod startPeriod,
      Room room,
      Section section) {
    if (teacherConflictCheck) {
      List<ClassPeriod> classPeriods = startPeriod.getTailPeriods(true).subList(
          0,
          section.getCourse().getPeriodLength());
      for (ClassPeriod period : classPeriods) {

        Program program = schedule.getProgram();
        Map<Room, Section> current = schedule.getStartingTimeTable().row(period);
        SetMultimap<Teacher, Table.Cell<ClassPeriod, Room, Section>> teachingAtTime = HashMultimap.create();

        Cell<ClassPeriod, Room, Section> assignment = Tables.immutableCell(period, room, section);
        Set<Teacher> forCourse = program.teachersForCourse(section.getCourse());

        for (Entry<Room, Section> entry : current.entrySet()) {
          Section currentSection = entry.getValue();
          Course course = currentSection.getCourse();
          Room currentRoom = entry.getKey();
          for (Teacher teacher : program.teachersForCourse(course)) {
            if (forCourse.contains(teacher)) {
              teachingAtTime.put(
                  teacher,
                  Tables.immutableCell(period, currentRoom, currentSection));
            }
          }
        }
        for (Teacher teacher : forCourse) {
          teachingAtTime.remove(teacher, assignment);
        }

        for (Entry<Teacher, Collection<Cell<ClassPeriod, Room, Section>>> teacherEntry : teachingAtTime.asMap()
            .entrySet()) {
          Collection<Cell<ClassPeriod, Room, Section>> assignments = teacherEntry.getValue();
          validator.validate(assignment, assignments, "teacher " + teacherEntry.getKey()
              + " must not be scheduled to teach more than one class at a time");
        }
      }
    }
  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule) {
    if (teacherConflictCheck) {
      super.validate(validator, schedule);
    }
  }
}
