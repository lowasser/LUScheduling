package org.learningu.scheduling.logic;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

import com.google.common.collect.Table.Cell;

public abstract class ScheduleLogic {
  public void validateStartingAt(
      ScheduleValidator validator,
      Schedule schedule,
      ClassPeriod period,
      Room room,
      Section section) {
  }

  public void validateOccurringAt(
      ScheduleValidator validator,
      Schedule schedule,
      ClassPeriod period,
      Room room,
      Section section) {
  }

  public void validate(ScheduleValidator validator, Schedule schedule) {
    for (Cell<ClassPeriod, Room, Section> assignment : schedule.getStartingTimeTable().cellSet()) {
      validateStartingAt(
          validator,
          schedule,
          assignment.getRowKey(),
          assignment.getColumnKey(),
          assignment.getValue());
    }
    for (Cell<ClassPeriod, Room, Section> assignment : schedule.getScheduleTable().cellSet()) {
      validateOccurringAt(
          validator,
          schedule,
          assignment.getRowKey(),
          assignment.getColumnKey(),
          assignment.getValue());
    }
  }
}
