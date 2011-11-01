package org.learningu.scheduling;

import org.learningu.scheduling.annotations.Initial;
import org.learningu.scheduling.modules.OptimizerModule;
import org.learningu.scheduling.modules.ScheduleLogicModule;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.Schedules;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;

import com.google.common.util.concurrent.FutureCallback;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

public final class AutoschedulingConfigModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new OptimizerModule());
    install(new ScheduleLogicModule());
    Multibinder<FutureCallback<Schedule>> callbackBinder =
        Multibinder.newSetBinder(binder(), new TypeLiteral<FutureCallback<Schedule>>() {});
    callbackBinder.addBinding().to(RoomPrettyPrinter.class).asEagerSingleton();
    callbackBinder.addBinding().to(ScheduleOutputCallback.class).asEagerSingleton();
    callbackBinder.addBinding().to(TeacherPrettyPrinter.class).asEagerSingleton();
  }

  @Provides
  @Initial
  Schedule initialSchedule(Schedule.Factory factory, SerialSchedule serial) {
    return Schedules.deserialize(factory, serial);
  }
}
