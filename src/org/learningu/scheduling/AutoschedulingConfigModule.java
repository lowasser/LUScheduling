package org.learningu.scheduling;

import java.util.concurrent.ExecutorService;

import org.learningu.scheduling.annotations.Initial;
import org.learningu.scheduling.annotations.SingleThread;
import org.learningu.scheduling.logic.LogicProvider;
import org.learningu.scheduling.logic.ScheduleLogic;
import org.learningu.scheduling.optimization.Annealer;
import org.learningu.scheduling.optimization.Optimizer;
import org.learningu.scheduling.optimization.OptimizerFactory;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.Schedules;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public final class AutoschedulingConfigModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new PassModule());
    bind(ScheduleLogic.class).toProvider(LogicProvider.class);
    bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class);
    install(new FactoryModuleBuilder().implement(
        new TypeLiteral<Optimizer<Schedule>>() {},
        new TypeLiteral<Annealer<Schedule>>() {}).build(
        Key.get(new TypeLiteral<OptimizerFactory<Schedule>>() {}, SingleThread.class)));
  }

  @Provides
  @Initial
  Schedule initialSchedule(Schedule.Factory factory, SerialSchedule serial) {
    return Schedules.deserialize(factory, serial);
  }
}
