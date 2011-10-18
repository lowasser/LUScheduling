package org.learningu.scheduling.annealing;

public interface OptimizerFactory<T> {
  Optimizer<T> createOptimizer(T initial, Scorer<T> scorer);
}
