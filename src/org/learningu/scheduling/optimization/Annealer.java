package org.learningu.scheduling.optimization;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

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
  Annealer(Perturber<T> perturber, Scorer<T> scorer, @Assisted TemperatureFunction tempFun,
      AcceptanceFunction acceptFun, Logger logger) {
    this.perturber = checkNotNull(perturber);
    this.scorer = checkNotNull(scorer);
    this.tempFun = checkNotNull(tempFun);
    this.acceptFun = checkNotNull(acceptFun);
    this.logger = logger;
  }

  @Override
  public Scorer<T> getScorer() {
    return scorer;
  }

  @Override
  public T iterate(int steps, T initial) {
    T current = initial;
    double currentScore = scorer.score(current);
    logger.log(Level.FINE, "Annealing for {0} steps; initial score is {1}", new Object[] { steps,
        currentScore });
    for (int i = 0; i < steps; i++) {
      double temp = tempFun.temperature(i, steps);
      logger.log(Level.FINE, "On has temperature {1}; best current score is {2}", new Object[] {
          i, temp, currentScore });
      T next = perturber.perturb(current, temp);
      double nextScore = scorer.score(next);
      logger.log(Level.FINER, "Score of new candidate is {0}", nextScore);
      if (acceptFun.acceptNewState(currentScore, nextScore, temp)) {
        logger.finer("Accepted new candidate");
        current = next;
        currentScore = nextScore;
      } else {
        logger.finer("Rejected new candidate");
      }
    }
    return current;
  }
}