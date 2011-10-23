package org.learningu.scheduling;

import java.util.List;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

import com.google.common.collect.Table;

public class Schedules {
  private Schedules() {
  }

  public static List<ClassPeriod> assignedPeriods(Course course, ClassPeriod start) {
    return start.getTailPeriods(course.getPeriodLength());
  }

  public static List<ClassPeriod> assignedPeriods(Table.Cell<ClassPeriod, Room, Section> start) {
    Section section = start.getValue();
    Course course = section.getCourse();
    ClassPeriod startPd = start.getRowKey();
    return assignedPeriods(course, startPd);
  }
}
