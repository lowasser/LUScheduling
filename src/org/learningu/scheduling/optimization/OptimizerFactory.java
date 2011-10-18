package org.learningu.scheduling.optimization;

public interface OptimizerFactory<T> {
  Optimizer<T> createOptimizer(T initial, Scorer<T> scorer);
}
