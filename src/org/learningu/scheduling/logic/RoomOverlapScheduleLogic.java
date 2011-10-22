package org.learningu.scheduling.logic;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.util.Condition;
import org.learningu.scheduling.util.Flag;

import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.inject.Inject;
import com.google.inject.name.Named;

final class RoomOverlapScheduleLogic implements ScheduleLogic {

  @Flag(
      value = "overlappingClassCheck",
      description = "Check that multi-period classes do not overlap with other classes in "
          + "the same room.",
      defaultValue = "true")
  final boolean overlappingClassCheck;

  @Inject
  RoomOverlapScheduleLogic(
      @Named("overlappingClassCheck") boolean overlappingClassCheck) {
    this.overlappingClassCheck = overlappingClassCheck;
  }

  @Override
  public Condition isValid(Condition parent, Schedule schedule) {
    /*
     * This is equivalent to verifying that the starting time table matches correctly with the full
     * schedule table.
     */
    Table<ClassPeriod, Room, Section> startTable = schedule.getStartingTimeTable();
    Table<ClassPeriod, Room, Section> schedTable = HashBasedTable.create(schedule.getScheduleTable());

    Condition noOverlaps = parent.createSubCondition(getClass().getName());

    noOverlaps.log(
        Level.FINEST,
        "Checking for correct match between start-table and schedule-table in schedule %s",
        schedule);

    for (Cell<ClassPeriod, Room, Section> cell : startTable.cellSet()) {
      ClassPeriod startPd = cell.getRowKey();
      Room room = cell.getColumnKey();
      Section section = cell.getValue();
      Course course = section.getCourse();
      List<ClassPeriod> targetPeriods = startPd.getTailPeriods(true);
      noOverlaps.verify(
          targetPeriods.size() >= course.getPeriodLength(),
          "Schedule assignment %s runs over the end of the time block",
          cell);
      if (targetPeriods.size() >= course.getPeriodLength()) {
        targetPeriods = targetPeriods.subList(0, course.getPeriodLength());
        for (ClassPeriod target : targetPeriods) {
          Section removed = schedTable.remove(target, room);
          noOverlaps.verify(
              Objects.equal(removed, section),
              "Expected %s to be scheduled in period %s in room %s, but was %s",
              section,
              target,
              room,
              removed);
        }
      }
    }

    noOverlaps.verify(
        schedTable.isEmpty(),
        "The following courses were scheduled in rooms but did not have a registered start time: %s",
        schedTable.cellSet());

    return noOverlaps;
  }

}
