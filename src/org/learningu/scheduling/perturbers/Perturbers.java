package org.learningu.scheduling.perturbers;

import com.google.common.collect.ImmutableList;

import java.util.Random;

import org.learningu.scheduling.optimization.Perturber;
import org.learningu.scheduling.perturbers.SerialPerturbers.ScaledPerturber;
import org.learningu.scheduling.perturbers.SerialPerturbers.SequencedPerturber;
import org.learningu.scheduling.perturbers.SerialPerturbers.SerialPerturberImpl;
import org.learningu.scheduling.schedule.Schedule;

/**
 * Utilities for converting between the protobuf-based {@link SequencedPerturber} values and
 * {@code Perturber<Schedule>} objects for use at runtime.
 * 
 * @author lowasser
 */
public final class Perturbers {
  private Perturbers() {
  }

  public static Perturber<Schedule> deserialize(SequencedPerturber serial) {
    ImmutableList.Builder<Perturber<Schedule>> sequenceBuilder = ImmutableList.builder();
    for (ScaledPerturber seq : serial.getPerturbList()) {
      sequenceBuilder.add(deserialize(seq));
    }
    final ImmutableList<Perturber<Schedule>> sequence = sequenceBuilder.build();
    return new Perturber<Schedule>() {
      @Override
      public Schedule perturb(Schedule initial, double temperature) {
        Schedule current = initial;
        for (Perturber<Schedule> seq : sequence) {
          current = seq.perturb(current, temperature);
        }
        // assert current.isCompletelyValid();
        return current;
      }
    };
  }

  private static Perturber<Schedule> deserialize(ScaledPerturber serial) {
    final Perturber<Schedule> delegate = deserialize(serial.getImpl());
    final double tempScale = serial.getTemperatureScale();
    return new Perturber<Schedule>() {
      @Override
      public Schedule perturb(Schedule initial, double temperature) {
        return delegate.perturb(initial, temperature * tempScale);
      }
    };
  }

  private static Perturber<Schedule> deserialize(SerialPerturberImpl serial) {
    Random random = new Random();
    switch (serial) {
      case DESTRUCTIVE:
        return new DestructivePerturber(random);
      case GREEDY:
        return new GreedyPerturber(random);
      case DESTRUCTIVE_BY_ROOM:
        return new DestructiveByRoomPerturber(random);
      case SWAPPING:
        return new SwappingPerturber(random);
      default:
        throw new AssertionError();
    }
  }
}
