package org.learningu.scheduling;

import java.util.concurrent.ExecutorService;

import org.learningu.scheduling.annotations.Initial;
import org.learningu.scheduling.logic.LogicProvider;
import org.learningu.scheduling.logic.ScheduleLogic;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.Schedules;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

public final class AutoschedulingConfigModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new OptimizerModule());
    install(new ScheduleLogicModule());
    bind(ScheduleLogic.class).toProvider(LogicProvider.class);
    bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Initial
  Schedule initialSchedule(Schedule.Factory factory, SerialSchedule serial) {
    return Schedules.deserialize(factory, serial);
  }
}
