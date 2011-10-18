package org.learningu.scheduling.optimization;

import java.util.Random;

import junit.framework.TestCase;

import org.learningu.scheduling.optimization.impl.QuadraticTemperatureFunction;
import org.learningu.scheduling.optimization.impl.StandardAcceptanceFunction;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Tests that annealing gets the correct answer on a simple use case. This test case may fail
 * spuriously, with low probability.
 * 
 * @author lowasser
 */
public class AnnealerTest extends TestCase {

  @SuppressWarnings("unused")
  public void testAnnealer() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Random.class).toInstance(new Random(0));
        bind(TemperatureFunction.class).to(QuadraticTemperatureFunction.class).asEagerSingleton();
        bind(double.class).annotatedWith(Names.named("TemperatureScale")).toInstance(1000.0);
        bind(Integer.class).annotatedWith(Names.named("Initial")).toInstance(-103);
        bind(AcceptanceFunction.class).to(StandardAcceptanceFunction.class).asEagerSingleton();
      }

      @Provides
      @Singleton
      public Scorer<Integer> scoreFunction() {
        return new Scorer<Integer>() {
          @Override
          public double score(Integer x) {
            return - (x - 6) * (x - 6);
          }
        };
      }

      @Provides
      @Singleton
      public Perturber<Integer> perturber(final Random gen) {
        return new Perturber<Integer>() {
          @Override
          public Integer perturb(Integer initial, double temperature) {
            int sign = gen.nextBoolean() ? 1 : -1;
            return initial + sign;
          }
        };
      }
    });

    Annealer<Integer> annealer = injector.getInstance(Key
        .get(new TypeLiteral<Annealer<Integer>>() {}));
    annealer.iterate(1000);
    assertEquals(6, annealer.getCurrentBest().intValue());
  }
}
