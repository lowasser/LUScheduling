package org.learningu.scheduling.scorers;

import org.learningu.scheduling.optimization.Scorer;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.scorers.SerialScorers.CompleteScorer;
import org.learningu.scheduling.scorers.SerialScorers.ScaledScorer;
import org.learningu.scheduling.scorers.SerialScorers.SerialScorerImpl;

import com.google.common.collect.ImmutableList;

public final class Scorers {
  private Scorers() {
  }

  public static Scorer<Schedule> deserialize(CompleteScorer serial) {
    ImmutableList.Builder<Scorer<Schedule>> componentsBuilder = ImmutableList.builder();
    for (ScaledScorer component : serial.getComponentList()) {
      componentsBuilder.add(deserialize(component));
    }
    final ImmutableList<Scorer<Schedule>> components = componentsBuilder.build();
    return new Scorer<Schedule>() {

      @Override
      public double score(Schedule input) {
        double total = 0;
        for (Scorer<Schedule> component : components) {
          total += component.score(input);
        }
        return total;
      }
    };
  }

  private static Scorer<Schedule> deserialize(ScaledScorer serial) {
    final Scorer<Schedule> base = deserialize(serial.getImpl());
    final double exponent = serial.getExponent();
    final double multiplier = serial.getMultiplier();
    return new Scorer<Schedule>() {
      @Override
      public double score(Schedule input) {
        return multiplier * Math.pow(base.score(input), exponent);
      }
    };
  }

  private static Scorer<Schedule> deserialize(SerialScorerImpl serial) {
    switch (serial) {
      case DISTINCT_CLASSES:
        return new DistinctClassesScorer();
      case EST_STUDENT_HOURS:
        return new EstimatedStudentHoursScorer();
      default:
        throw new AssertionError();
    }
  }

}
