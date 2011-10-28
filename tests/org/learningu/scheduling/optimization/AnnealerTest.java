package org.learningu.scheduling.optimization;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.joda.time.Period;
import org.learningu.scheduling.modules.OptimizerModule;

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
        bind(ExecutorService.class).toInstance(Executors.newSingleThreadExecutor());
        bind(new TypeLiteral<Optimizer<Double>>() {}).to(
            new TypeLiteral<ConcurrentOptimizer<Double>>() {});
        install(new FactoryModuleBuilder().implement(
            new TypeLiteral<Optimizer<Double>>() {},
            new TypeLiteral<Annealer<Double>>() {}).build(
            new TypeLiteral<OptimizerFactory<Double>>() {}));
        bind(TemperatureFunction.class).annotatedWith(Names.named("primaryTempFun")).toInstance(
            OptimizerModule.LINEAR_FUNCTION);
        bind(TemperatureFunction.class).annotatedWith(Names.named("subTempFun")).toInstance(
            OptimizerModule.LINEAR_FUNCTION);
        bind(AcceptanceFunction.class).to(StandardAcceptanceFunction.class).asEagerSingleton();
        bindConstant().annotatedWith(Names.named("stepsPerOptimizerIteration")).to(10);
        bindConstant().annotatedWith(Names.named("nThreads")).to(4);
        bind(Period.class).annotatedWith(Names.named("iterationTimeout")).toInstance(
            Period.hours(1));

      }

      @Provides
      @Named("nSubOptimizers")
      int nSubOptimizers() {
        return 4;
      }

      @Provides
      @Named("subOptimizerSteps")
      int subOptimizerSteps() {
        return 100;
      }

      @Provides
      @Singleton
      public Scorer<Double> scoreFunction() {
        return new Scorer<Double>() {
          @Override
          public double score(Double x) {
            double x2 = x * x;
            double x3 = x2 * x;
            double f = x3 + x2 - x + 1;
            return -f * f;
          }
        };
      }

      @Provides
      @Singleton
      public Perturber<Double> perturber(final Random gen) {
        return new Perturber<Double>() {
          @Override
          public Double perturb(Double initial, double temperature) {
            return initial + (gen.nextGaussian() * temperature);
          }
        };
      }
    });

    Optimizer<Double> optimizer = injector.getInstance(Key
        .get(new TypeLiteral<Optimizer<Double>>() {}));
    double initial = 25.0;
    double opt = optimizer.iterate(50, initial);
    assertEquals(-1.83929, opt, 0.002);
  }
}
