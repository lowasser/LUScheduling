package org.learningu.scheduling.annealing;

/**
 * A tool for incrementally perturbing points in some space.
 * 
 * @author lowasser
 */
public interface Perturber<T> {
  /**
   * Perturbs the initial input, by an amount that does not increase as the temperature approaches
   * 0 from above.
   */
  T perturb(T initial, double temperature);
}
