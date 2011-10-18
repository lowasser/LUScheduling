package org.learningu.scheduling.optimization;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.name.Named;

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

  private T current;
  private double currentScore;

  private T best;
  private double bestScore;

  @Inject
  Annealer(Perturber<T> perturber, Scorer<T> scorer, TemperatureFunction tempFun,
      AcceptanceFunction acceptFun, @Named("Initial") T initial) {
    this.perturber = checkNotNull(perturber);
    this.scorer = checkNotNull(scorer);
    this.tempFun = checkNotNull(tempFun);
    this.acceptFun = checkNotNull(acceptFun);
    this.best = this.current = checkNotNull(initial);
    this.currentScore = this.bestScore = scorer.score(initial);
  }

  @Override
  public void iterate(int steps) {
    for (int step = 0; step < steps; step++) {
      double temp = tempFun.temperature(step, steps);
      T newCandidate = perturber.perturb(current, temp);
      updateWithCandidate(newCandidate, temp);
    }
  }

  @Override
  public T getCurrentBest() {
    return best;
  }

  boolean updateWithCandidate(T newCandidate, double temperature) {
    checkNotNull(newCandidate);
    checkArgument(temperature > 0);

    double newCandidateScore = scorer.score(newCandidate);

    if (newCandidateScore > bestScore) {
      best = newCandidate;
      bestScore = newCandidateScore;
    }
    if (acceptFun.acceptNewState(currentScore, newCandidateScore, temperature)) {
      current = newCandidate;
      currentScore = newCandidateScore;
      return true;
    }
    return false;
  }

  @Override
  public Scorer<T> getScorer() {
    return scorer;
  }

  @Override
  public boolean updateWithCandidate(T newCandidate) {
    double newCandidateScore = scorer.score(newCandidate);

    if (newCandidateScore > bestScore) {
      best = newCandidate;
      bestScore = newCandidateScore;
    }

    if (newCandidateScore > currentScore) {
      current = newCandidate;
      currentScore = newCandidateScore;
      return true;
    } else {
      return false;
    }
  }
}