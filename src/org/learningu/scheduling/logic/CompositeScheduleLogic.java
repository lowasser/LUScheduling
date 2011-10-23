package org.learningu.scheduling.logic;

import java.util.List;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

final class CompositeScheduleLogic extends ScheduleLogic {
  private final List<ScheduleLogic> logics;

  @Inject
  CompositeScheduleLogic(@CombinedLogics Iterable<ScheduleLogic> logics) {
    this.logics = ImmutableList.copyOf(logics);
  }

  @Override
  public void validateStartingAt(
      ScheduleValidator validator,
      Schedule schedule,
      ClassPeriod period,
      Room room,
      Section section) {
    for (ScheduleLogic logic : logics) {
      logic.validateStartingAt(validator, schedule, period, room, section);
    }
  }

  @Override
  public void validateOccurringAt(
      ScheduleValidator validator,
      Schedule schedule,
      ClassPeriod period,
      Room room,
      Section section) {
    for (ScheduleLogic logic : logics) {
      logic.validateOccurringAt(validator, schedule, period, room, section);
    }
  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule) {
    for (ScheduleLogic logic : logics) {
      logic.validate(validator, schedule);
    }
  }

}
