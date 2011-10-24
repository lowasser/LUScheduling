package org.learningu.scheduling.scorers;

import org.learningu.scheduling.optimization.Scorer;

/**
 * A rescaled scorer with some multiplier.
 * 
 * @author lowasser
 */
public final class ScaledScorer<T> implements Scorer<T> {
  public static <T> ScaledScorer<T> create(double scale, Scorer<T> scorer) {
    return new ScaledScorer<T>(scale, scorer);
  }

  private final double scale;

  private final Scorer<T> scorer;

  private ScaledScorer(double scale, Scorer<T> scorer) {
    this.scale = scale;
    this.scorer = scorer;
  }

  @Override
  public double score(T input) {
    return scale * scorer.score(input);
  }
}
