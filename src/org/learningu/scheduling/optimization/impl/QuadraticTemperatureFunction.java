package org.learningu.scheduling.optimization.impl;

import org.learningu.scheduling.optimization.TemperatureFunction;

public class QuadraticTemperatureFunction implements TemperatureFunction {

  @Override
  public double temperature(int currentStep, int nSteps) {
    double reScale = 1.0 / (nSteps * nSteps);
    int delta = nSteps - currentStep;
    return (delta * delta) * reScale;
  }

}
