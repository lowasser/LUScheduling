package org.learningu.scheduling;

import java.util.concurrent.Callable;

import org.joda.time.Duration;
import org.learningu.scheduling.flags.Flag;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.optimization.ConcurrentOptimizer;
import org.learningu.scheduling.schedule.Schedule;

import com.google.inject.Inject;

final class Autoscheduler implements Callable<Schedule> {
  private final Program program;

  private final Schedule initialSchedule;

  private final ConcurrentOptimizer<Schedule> optimizer;

  @Inject(optional = true)
  @Flag(name = "optimizeTime")
  private Duration optimizerTime = Duration.standardMinutes(1);

  @Inject
  Autoscheduler(
      Program program,
      Schedule initialSchedule,
      ConcurrentOptimizer<Schedule> optimizer) {
    this.program = program;
    this.initialSchedule = initialSchedule;
    this.optimizer = optimizer;
  }

  public Program getProgram() {
    return program;
  }

  public Schedule getInitialSchedule() {
    return initialSchedule;
  }

  public ConcurrentOptimizer<Schedule> getOptimizer() {
    return optimizer;
  }

  @Override
  public Schedule call() throws Exception {
    return optimizer.iterate(optimizerTime, initialSchedule);
  }
}
