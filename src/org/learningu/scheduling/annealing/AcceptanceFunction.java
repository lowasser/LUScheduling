package org.learningu.scheduling.annealing;

/**
 * An acceptance function for moving to a new state based on the difference in their scores and the
 * temperature at the time.
 * 
 * @author lowasser
 */
public interface AcceptanceFunction {
  boolean acceptNewState(double originalScore, double newScore, double temperature);
}
