package org.learningu.scheduling.logic;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

/**
 * A schedule logic that applies several sub-logics in sequence.
 * 
 * @author lowasser
 */
public final class ChainedScheduleLogic extends ScheduleLogic {
  public static ChainedScheduleLogic create(ScheduleLogic... logics) {
    return new ChainedScheduleLogic(Arrays.asList(logics));
  }

  public static ChainedScheduleLogic create(Iterable<? extends ScheduleLogic> logics) {
    return new ChainedScheduleLogic(logics);
  }

  private final ImmutableList<ScheduleLogic> logics;

  @Inject
  private ChainedScheduleLogic(Iterable<? extends ScheduleLogic> logics) {
    this.logics = ImmutableList.copyOf(logics);
  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    if (assignment.isLocked()) {
      return;
    }
    for (ScheduleLogic logic : logics) {
      logic.validate(validator, schedule, assignment);
    }
  }

  @Override
  public
      void
      validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
    for (ScheduleLogic logic : logics) {
      logic.validate(validator, schedule, assignment);
    }
    Logger.getLogger("org.learningu.scheduling.logic.ChainedScheduleLogic").log(
        Level.FINEST,
        "Chained schedule logic: {0}",
        validator);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("logics", logics).toString();
  }
}
