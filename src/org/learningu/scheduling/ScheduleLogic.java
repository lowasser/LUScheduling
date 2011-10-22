package org.learningu.scheduling;

import org.learningu.scheduling.util.Condition;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultScheduleLogic.class)
public interface ScheduleLogic {
  Condition isValid(Schedule schedule);
}
