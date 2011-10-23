package org.learningu.scheduling.logic;

import java.util.Set;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.util.Flag;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
final class DuplicateSectionsLogic extends ScheduleLogic {

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
  public void validateStartingAt(
      ScheduleValidator validator,
      Schedule schedule,
      ClassPeriod period,
      Room room,
      Section section) {
    if (doublyScheduledSectionsCheck) {
      Set<Table.Cell<ClassPeriod, Room, Section>> matchingAssignments = Sets.newHashSet();
      for (Cell<ClassPeriod, Room, Section> assignment : schedule.getStartingTimeTable().cellSet()) {
        if (assignment.getValue().equals(section)) {
          matchingAssignments.add(assignment);
        }
      }
      Cell<ClassPeriod, Room, Section> assignment = Tables.immutableCell(period, room, section);
      matchingAssignments.remove(assignment);
      validator.validate(
          assignment,
          matchingAssignments,
          "a single section of a course must not be scheduled more than once");
    }
  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule) {
    if (doublyScheduledSectionsCheck) {
      super.validate(validator, schedule);
    }
  }
}
