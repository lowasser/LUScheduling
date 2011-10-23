package org.learningu.scheduling.logic;

import java.util.List;
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
import com.google.inject.name.Named;

final class RoomOverlapScheduleLogic extends ScheduleLogic {

  @Flag(
      value = "overlappingClassCheck",
      description = "Check that multi-period classes do not overlap with other classes in "
          + "the same room.",
      defaultValue = "true")
  final boolean overlappingClassCheck;

  @Inject
  RoomOverlapScheduleLogic(@Named("overlappingClassCheck") boolean overlappingClassCheck) {
    this.overlappingClassCheck = overlappingClassCheck;
  }

  @Override
  public void validateStartingAt(
      ScheduleValidator validator,
      Schedule schedule,
      ClassPeriod period,
      Room room,
      Section section) {
    if (overlappingClassCheck) {
      Table<ClassPeriod, Room, Section> startTable = schedule.getStartingTimeTable();
      // First, test for no classes that would start during this class, except possibly ourselves.
      Set<Table.Cell<ClassPeriod, Room, Section>> overlappingStart = Sets.newHashSet();
      int periods = section.getCourse().getPeriodLength();
      List<ClassPeriod> presentPeriods = period.getTailPeriods(true).subList(0, periods);
      for (ClassPeriod pd : presentPeriods) {
        Section startingSection = startTable.get(pd, room);
        if (startingSection != null) {
          overlappingStart.add(Tables.immutableCell(pd, room, startingSection));
        }
      }
      Cell<ClassPeriod, Room, Section> assignment = Tables.immutableCell(period, room, section);
      overlappingStart.remove(assignment);
      validator.validate(
          assignment,
          overlappingStart,
          "courses may not keep a room while other courses are using it");
    }

  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule) {
    if (overlappingClassCheck) {
      super.validate(validator, schedule);
    }
  }

}
