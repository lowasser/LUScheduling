package org.learningu.scheduling.logic;

import org.learningu.scheduling.PresentAssignment;
import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.StartAssignment;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

final class ChainedScheduleLogic extends ScheduleLogic {
  private final ImmutableList<ScheduleLogic> logics;

  @Inject
  private ChainedScheduleLogic(Iterable<? extends ScheduleLogic> logics) {
    this.logics = ImmutableList.copyOf(logics);
  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    for (ScheduleLogic logic : logics) {
      logic.validate(validator, schedule, assignment);
    }
  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
    for (ScheduleLogic logic : logics) {
      logic.validate(validator, schedule, assignment);
    }
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("logics", logics).toString();
  }
}
