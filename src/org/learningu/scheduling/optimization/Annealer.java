package org.learningu.scheduling.optimization;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Logger;

import com.google.inject.Inject;

/**
 * An optimizer implementation based on simulated annealing techniques.
 * 
 * @author lowasser
 */
public final class Annealer<T> implements Optimizer<T> {
  private final Perturber<T> perturber;

  private final Scorer<T> scorer;

  private final TemperatureFunction tempFun;

  private final AcceptanceFunction acceptFun;

  private final Logger logger;

  @Inject
  Annealer(Perturber<T> perturber, Scorer<T> scorer, TemperatureFunction tempFun,
      AcceptanceFunction acceptFun, Logger logger) {
    this.perturber = checkNotNull(perturber);
    this.scorer = checkNotNull(scorer);
    this.tempFun = checkNotNull(tempFun);
    this.acceptFun = checkNotNull(acceptFun);
    this.logger = checkNotNull(logger);
  }

  @Override
  public Scorer<T> getScorer() {
    return scorer;
  }

  @Override
  public T iterate(int steps, T initial) {
    // TODO Auto-generated method stub
    return null;
  }
}