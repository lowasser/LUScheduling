package org.learningu.scheduling.optimization;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.logging.Level;
import java.util.logging.Logger;

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

  private final Stopwatch stopwatch;

  @Inject
  Annealer(
      Perturber<T> perturber,
      Scorer<T> scorer,
      @Assisted TemperatureFunction tempFun,
      AcceptanceFunction acceptFun,
      Logger logger,
      Stopwatch stopwatch) {
    this.perturber = checkNotNull(perturber);
    this.scorer = checkNotNull(scorer);
    this.tempFun = checkNotNull(tempFun);
    this.acceptFun = checkNotNull(acceptFun);
    this.logger = logger;
    this.stopwatch = stopwatch;
  }

  @Override
  public Scorer<T> getScorer() {
    return scorer;
  }

  @Override
  public T iterate(int steps, T initial) {
    stopwatch.start();
    T current = initial;
    double currentScore = scorer.score(current);
    T best = current;
    double bestScore = currentScore;
    logger.log(Level.FINE, "Annealing for {0} steps; initial score is {1}", new Object[] { steps,
        currentScore });
    for (int i = 0; i < steps; i++) {
      double temp = tempFun.temperature(i, steps);
      logger.log(
          Level.FINE,
          "On step {0}; temperature is {1}; best current score is {2}",
          new Object[] { i, temp, currentScore });
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
      if (nextScore > bestScore) {
        best = next;
        bestScore = nextScore;
      }
    }
    stopwatch.stop();
    /*
    logger.log(
        Level.FINE,
        "Single-threaded annealing step took {0}",
        Duration
            .millis(stopwatch.elapsedMillis())
            .toPeriod()
            .toString(Converters.PERIOD_FORMATTER));*/
    return best;
  }
}