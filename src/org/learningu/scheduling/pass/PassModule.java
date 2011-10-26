package org.learningu.scheduling.pass;

import org.learningu.scheduling.Pass.OptimizerSpec;
import org.learningu.scheduling.Pass.SerialTemperatureFunction;
import org.learningu.scheduling.optimization.TemperatureFunction;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public final class PassModule extends AbstractModule {
  public static final TemperatureFunction LINEAR_FUNCTION = new TemperatureFunction() {
    @Override
    public double temperature(int currentStep, int nSteps) {
      return ((double) (nSteps - 1 - currentStep)) / nSteps;
    }
  };

  private final OptimizerSpec spec;

  public PassModule(OptimizerSpec spec) {
    this.spec = spec;
  }

  @Override
  protected void configure() {
    bind(TemperatureFunction.class).annotatedWith(Names.named("primaryTempFun")).toInstance(
        deserialize(spec.getPrimaryTempFun()));
    bind(TemperatureFunction.class).annotatedWith(Names.named("subTempFun")).toInstance(
        deserialize(spec.getSubTempFun()));
  }

  @Provides
  @Named("primaryTempFun")
  TemperatureFunction primaryTemperatureFunction() {
    return deserialize(spec.getPrimaryTempFun());
  }

  @Provides
  @Named("subTempFun")
  TemperatureFunction subTemperatureFunction() {
    return deserialize(spec.getSubTempFun());
  }

  @Provides
  @Named("subOptimizerSteps")
  int subOptimizerSteps() {
    return spec.getSubOptimizerSteps();
  }

  @Provides
  @Named("nSubOptimizers")
  int nSubOptimizers() {
    return spec.getSubOptimizerSteps();
  }

  private static TemperatureFunction deserialize(SerialTemperatureFunction serial) {
    switch (serial) {
      case LINEAR:
        return LINEAR_FUNCTION;
      default:
        throw new AssertionError();
    }
  }
}
