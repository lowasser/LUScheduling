package org.learningu.scheduling.optimization.impl;

import org.learningu.scheduling.optimization.TemperatureFunction;

public final class LinearTemperatureFunction implements TemperatureFunction {

  @Override
  public double temperature(int currentStep, int nSteps) {
    return (((double) (nSteps - currentStep)) / nSteps);
  }
}
