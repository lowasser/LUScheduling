package org.learningu.scheduling.annealing;

/**
 * A tool for incrementally optimizing the value of a scoring function.
 * 
 * @author lowasser
 */
public interface Optimizer<T> {
  Scorer<T> getScorer();

  void iterate(int steps);

  T getCurrentBest();

  boolean updateWithCandidate(T newCandidate);
}
