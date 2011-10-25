package org.learningu.scheduling.optimization;

/**
 * A tool for incrementally optimizing the value of a scoring function.
 * 
 * @author lowasser
 */
public interface Optimizer<T> {
  Scorer<T> getScorer();

  T iterate(int steps, T initial);
}
