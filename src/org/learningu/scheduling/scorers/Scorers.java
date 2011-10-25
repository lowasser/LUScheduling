package org.learningu.scheduling.scorers;

import junit.framework.AssertionFailedError;

import org.learningu.scheduling.optimization.Scorer;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.scorers.SerialScorers.CompleteScorer;
import org.learningu.scheduling.scorers.SerialScorers.ScaledScorer;
import org.learningu.scheduling.scorers.SerialScorers.SerialScorerImpl;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public final class Scorers {
  private static final ImmutableMap<SerialScorerImpl, Class<? extends Scorer<Schedule>>> impls =
      ImmutableMap.<SerialScorerImpl, Class<? extends Scorer<Schedule>>> builder()
          .put(SerialScorerImpl.DISTINCT_CLASSES, DistinctClassesScorer.class)
          .put(SerialScorerImpl.EST_STUDENT_HOURS, EstimatedStudentHoursScorer.class)
          .build();

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
    Class<? extends Scorer<Schedule>> impl = impls.get(serial);
    if (impl == null) {
      throw new AssertionFailedError("Do not know how to construct " + serial.name());
    }
    try {
      return impl.newInstance();
    } catch (InstantiationException e) {
      throw Throwables.propagate(e);
    } catch (IllegalAccessException e) {
      throw Throwables.propagate(e);
    }
  }

}
