package org.learningu.scheduling.logic;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.util.Condition;
import org.learningu.scheduling.util.Flag;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
final class TeacherConflictsScheduleLogic implements ScheduleLogic {

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
  public Condition isValid(Condition teacherConflicts, Schedule schedule) {
    if (teacherConflictCheck) {
      Program program = schedule.getProgram();
      Table<ClassPeriod, Room, Section> table = schedule.getScheduleTable();
      teacherConflicts.getLogger().log(
          Level.FINEST,
          "Checking for teacher conflicts in schedule %s",
          schedule);

      for (Entry<ClassPeriod, Map<Room, Section>> scheduleEntry : table.rowMap().entrySet()) {
        ClassPeriod block = scheduleEntry.getKey();
        Map<Room, Section> roomAssignments = scheduleEntry.getValue();

        SetMultimap<Teacher, Section> coursesTeachingNow = HashMultimap.create(
            roomAssignments.size(),
            4);

        for (Section s : roomAssignments.values()) {
          for (Teacher t : program.teachersForCourse(s.getCourse())) {
            coursesTeachingNow.put(t, s);
          }
        }

        for (Entry<Teacher, Collection<Section>> teacherAssignments : coursesTeachingNow.asMap()
            .entrySet()) {
          Teacher t = teacherAssignments.getKey();
          Collection<Section> teaching = teacherAssignments.getValue();
          teacherConflicts.verify(
              teaching.size() <= 1,
              "Teacher %s has conflicts between %s classes in block %s: %s",
              t,
              teaching.size(),
              block,
              teaching);
        }
      }
    }
    return teacherConflicts;
  }

}
