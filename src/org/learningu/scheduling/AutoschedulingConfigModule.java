package org.learningu.scheduling;

import java.util.concurrent.ExecutorService;

import org.learningu.scheduling.Pass.OptimizerSpec;
import org.learningu.scheduling.Pass.SerialTemperatureFunction;
import org.learningu.scheduling.annotations.Initial;
import org.learningu.scheduling.annotations.SingleThread;
import org.learningu.scheduling.logic.LogicProvider;
import org.learningu.scheduling.logic.ScheduleLogic;
import org.learningu.scheduling.optimization.AcceptanceFunction;
import org.learningu.scheduling.optimization.Annealer;
import org.learningu.scheduling.optimization.ConcurrentOptimizer;
import org.learningu.scheduling.optimization.Optimizer;
import org.learningu.scheduling.optimization.OptimizerFactory;
import org.learningu.scheduling.optimization.Perturber;
import org.learningu.scheduling.optimization.Scorer;
import org.learningu.scheduling.optimization.StandardAcceptanceFunction;
import org.learningu.scheduling.optimization.TemperatureFunction;
import org.learningu.scheduling.perturbers.Perturbers;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.Schedules;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;
import org.learningu.scheduling.scorers.Scorers;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;

public final class AutoschedulingConfigModule extends AbstractModule {

  public static final TemperatureFunction LINEAR_FUNCTION = new TemperatureFunction() {
    @Override
    public double temperature(int currentStep, int nSteps) {
      return ((double) (nSteps - 1 - currentStep)) / nSteps;
    }
  };

  @Override
  protected void configure() {
    bind(ScheduleLogic.class).toProvider(LogicProvider.class);
    bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class).in(Scopes.SINGLETON);
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

  @Provides
  Optimizer<Schedule> optimizer(ConcurrentOptimizer<Schedule> opt) {
    return opt;
  }

  @Provides
  @Singleton
  Scorer<Schedule> scorer(OptimizerSpec spec) {
    return Scorers.deserialize(spec.getScorer());
  }

  @Provides
  Perturber<Schedule> perturber(OptimizerSpec spec) {
    return Perturbers.deserialize(spec.getPerturber());
  }

  @Provides
  @Named("primaryTempFun")
  TemperatureFunction primaryTemperatureFunction(OptimizerSpec spec) {
    return deserialize(spec.getPrimaryTempFun());
  }

  @Provides
  @Named("subTempFun")
  TemperatureFunction subTemperatureFunction(OptimizerSpec spec) {
    return deserialize(spec.getSubTempFun());
  }

  @Provides
  @Named("subOptimizerSteps")
  int subOptimizerSteps(OptimizerSpec spec) {
    return spec.getSubOptimizerSteps();
  }

  @Provides
  @Named("nSubOptimizers")
  int nSubOptimizers(OptimizerSpec spec) {
    return spec.getSubOptimizerSteps();
  }

  @Provides
  AcceptanceFunction acceptFun(
      OptimizerSpec spec,
      Provider<StandardAcceptanceFunction> standardProv) {
    switch (spec.getSubAcceptFun()) {
      case STANDARD_EXPONENTIAL:
        return standardProv.get();
      default:
        throw new AssertionError();
    }
  }

  private static TemperatureFunction deserialize(SerialTemperatureFunction serial) {
    switch (serial) {
      case LINEAR:
        return AutoschedulingConfigModule.LINEAR_FUNCTION;
      default:
        throw new AssertionError();
    }
  }
}
