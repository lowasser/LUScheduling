package org.learningu.scheduling.logic;

import java.util.Map;
import java.util.logging.Level;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.util.Condition;
import org.learningu.scheduling.util.Flag;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
final class DuplicateSectionsLogic implements ScheduleLogic {

  @Flag(
      value = "doublyScheduledSectionsCheck",
      description = "Check for sections appearing more than once in the schedule.",
      defaultValue = "true")
  final boolean doublyScheduledSectionsCheck;

  @Inject
  DuplicateSectionsLogic(
      @Named("doublyScheduledSectionsCheck") boolean doublyScheduledSectionsCheck) {
    this.doublyScheduledSectionsCheck = doublyScheduledSectionsCheck;
  }

  @Override
  public Condition isValid(Condition duplicateCourses, Schedule schedule) {
    Table<ClassPeriod, Room, Section> table = schedule.getStartingTimeTable();
    Map<Section, Table.Cell<ClassPeriod, Room, Section>> cellMap = Maps.newHashMapWithExpectedSize(table.size());

    if (doublyScheduledSectionsCheck) {
      duplicateCourses.log(Level.FINEST, "Checking for duplicate courses in schedule %s", schedule);

      for (Cell<ClassPeriod, Room, Section> cell : table.cellSet()) {
        Section s = cell.getValue();
        Cell<ClassPeriod, Room, Section> previous = cellMap.get(s);
        duplicateCourses.verify(
            previous == null,
            "Section %s is scheduled twice: %s and %s",
            s,
            cell,
            previous);
        cellMap.put(s, cell);
      }
    }

    return duplicateCourses;
  }

}
