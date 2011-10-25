package org.learningu.scheduling.optimization;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.learningu.scheduling.Flag;
import org.learningu.scheduling.FlagsModule;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public final class OptimizationModule extends AbstractModule {

  @Override
  protected void configure() {
    install(FlagsModule.create(ConcurrentOptimizer.class));
    install(FlagsModule.create(ExecutorServiceProvider.class));
    install(FlagsModule.create(TemperatureFlags.class));
    bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class).asEagerSingleton();
  }

  @Provides
  @Singleton
  ListeningExecutorService listening(ExecutorService service) {
    return MoreExecutors.listeningDecorator(service);
  }

  enum TempFunImpl implements TemperatureFunction {
    LINEAR {
      @Override
      public double temperature(int currentStep, int nSteps) {
        return ((double) (nSteps - 1 - currentStep)) / nSteps;
      }
    };
  }

  static final class TemperatureFlags {
    @Flag(
        value = "primaryTempFun",
        description = "Temperature function for the primary optimizer",
        defaultValue = "LINEAR")
    final TempFunImpl primaryTempFun;

    @Flag(
        value = "subTempFun",
        description = "Temperature function for the sub-optimizers",
        defaultValue = "LINEAR")
    final TempFunImpl subTempFun;

    @Inject
    TemperatureFlags(TempFunImpl primaryTempFun, TempFunImpl subTempFun) {
      this.primaryTempFun = primaryTempFun;
      this.subTempFun = subTempFun;
    }
  }

  @Provides
  @Named("primaryTempFun")
  TemperatureFunction primTempFun(TemperatureFlags flags) {
    return flags.primaryTempFun;
  }

  @Provides
  @Named("subTempFun")
  TemperatureFunction subTempFun(TemperatureFlags flags) {
    return flags.subTempFun;
  }

  static final class ExecutorServiceProvider implements Provider<ExecutorService> {
    @Flag(
        value = "nThreads",
        defaultValue = "1",
        description = "Number of threads to use in concurrent optimization")
    private final int nThreads;

    @Inject
    ExecutorServiceProvider(@Named("nThreads") int nThreads) {
      this.nThreads = nThreads;
    }

    @Override
    public ExecutorService get() {
      return Executors.newFixedThreadPool(nThreads);
    }
  }

}
