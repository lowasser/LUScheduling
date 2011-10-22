package org.learningu.scheduling.logic;

import java.util.List;
import java.util.logging.Level;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.util.Condition;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

final class CompositeScheduleLogic implements ScheduleLogic {
  private final List<ScheduleLogic> logics;

  @Inject
  CompositeScheduleLogic(@CombinedLogics Iterable<ScheduleLogic> logics) {
    this.logics = ImmutableList.copyOf(logics);
  }

  @Override
  public Condition isValid(Condition valid, Schedule schedule) {
    for (ScheduleLogic logic : logics) {
      valid.log(Level.FINEST, "Using sub-logic %s", logic);
      logic.isValid(valid.createSubCondition(logic.toString()), schedule);
    }
    return valid;
  }

}
