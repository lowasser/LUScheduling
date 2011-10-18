package org.learningu.scheduling.annealing;

/**
 * A scoring function on some space of inputs which can be optimized.
 * 
 * @author lowasser
 */
public interface Scorer<T> {
  double score(T input);
}
