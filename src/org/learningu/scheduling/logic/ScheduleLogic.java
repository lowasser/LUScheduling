package org.learningu.scheduling.logic;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.util.Condition;

public interface ScheduleLogic {
  Condition isValid(Condition parent, Schedule schedule);
}
