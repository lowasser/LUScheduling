package org.learningu.scheduling.pass;

import org.learningu.scheduling.Schedule;

public interface Pass {
  public Schedule optimize(Schedule schedule);
}
