package org.learningu.scheduling.optimization;

public interface OptimizerFactory<T> {
  Optimizer<T> create(TemperatureFunction tempFun);
}
