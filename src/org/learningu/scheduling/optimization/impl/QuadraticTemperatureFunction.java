package org.learningu.scheduling.optimization.impl;

import static com.google.common.base.Preconditions.checkArgument;

import org.learningu.scheduling.optimization.TemperatureFunction;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class QuadraticTemperatureFunction implements TemperatureFunction {
  private final double scale;

  @Inject
  QuadraticTemperatureFunction(@Named("TemperatureScale") double scale) {
    this.scale = scale;
    checkArgument(scale > 0, "Temperature scale must be > 0");
  }

  @Override
  public double temperature(int currentStep, int nSteps) {
    double reScale = scale / (nSteps * nSteps);
    int delta = nSteps - currentStep;
    return (delta * delta) * reScale;
  }

}
