package org.learningu.scheduling.optimization;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.Duration;
import org.learningu.scheduling.annotations.SingleThread;
import org.learningu.scheduling.flags.Flag;

public final class ConcurrentOptimizer<T> implements Optimizer<T> {
  private final Scorer<T> scorer;

  private final OptimizerFactory<T> optimizerFactory;

  private final int nSubOptimizers;

  private final ExecutorService service;

  private final int subOptimizerSteps;

  @Inject(optional = true)
  @Flag(
      name = "iterationTimeout",
      description = "Maximum timeout for each concurrent optimizer iteration")
  private Duration iterTimeout = Duration.standardSeconds(10);

  private final Logger logger;

  private final TemperatureFunction primaryTempFun;

  private final TemperatureFunction subTempFun;

  @Inject
  ConcurrentOptimizer(
      Scorer<T> scorer,
      @SingleThread OptimizerFactory<T> optimizerProvider,
      @Named("primaryTempFun") TemperatureFunction primaryTempFun,
      @Named("subTempFun") TemperatureFunction subTempFun,
      @Named("nSubOptimizers") int nSubOptimizers,
      ExecutorService service,
      @Named("subOptimizerSteps") int subOptimizerSteps,
      Logger logger) {
    this.scorer = scorer;
    this.optimizerFactory = optimizerProvider;
    this.nSubOptimizers = nSubOptimizers;
    this.service = service;
    this.subOptimizerSteps = subOptimizerSteps;
    this.logger = logger;
    this.primaryTempFun = primaryTempFun;
    this.subTempFun = subTempFun;
  }

  @Override
  public Scorer<T> getScorer() {
    return scorer;
  }

  @Override
  public T iterate(int steps, T initial) {
    long timeoutMillis = iterTimeout.getMillis();
    T currentBest = initial;
    double currentBestScore = scorer.score(initial);
    for (int step = 0; step < steps; step++) {
      logger.log(Level.INFO, "On iteration step {0}, current best has score {1}", new Object[] {
          step, currentBestScore });
      List<Callable<T>> independentThreads = Lists.newArrayListWithCapacity(nSubOptimizers);
      double temp = primaryTempFun.temperature(step, steps);
      for (int i = 0; i < nSubOptimizers; i++) {
        independentThreads.add(runSingleThreadPass(currentBest, temp));
      }
      try {
        List<Future<T>> futures = service.invokeAll(independentThreads);

        for (Future<T> future : futures) {
          try {
            T better = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            double betterScore = scorer.score(better);
            if (betterScore > currentBestScore) {
              currentBest = better;
              currentBestScore = betterScore;
            }
          } catch (TimeoutException e) {
            logger.log(Level.WARNING, "Sub-optimizer timed out.  Skipping.");
          } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Sub-optimizer threw an exception.  Skipping.", e.getCause());
          }
        }
      } catch (InterruptedException e) {
        logger.log(Level.WARNING, "Thread interrupted, returning current best.");
        break;
      }
    }
    return currentBest;
  }

  public T iterate(Duration duration, T initial) {
    long timeoutMillis = iterTimeout.getMillis();
    long start = System.currentTimeMillis();
    long dur = duration.getMillis();
    T currentBest = initial;
    double currentBestScore = scorer.score(initial);
    for (int step = 0; System.currentTimeMillis() - start < dur; step++) {
      logger.log(Level.INFO, "On iteration step {0}, current best has score {1}", new Object[] {
          step, currentBestScore });
      List<Callable<T>> independentThreads = Lists.newArrayListWithCapacity(nSubOptimizers);
      double temp = primaryTempFun.temperature(
          (int) (System.currentTimeMillis() - start),
          (int) dur);
      for (int i = 0; i < nSubOptimizers; i++) {
        independentThreads.add(runSingleThreadPass(currentBest, temp));
      }
      try {
        List<Future<T>> futures = service.invokeAll(independentThreads);

        for (Future<T> future : futures) {
          try {
            T better = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            double betterScore = scorer.score(better);
            if (betterScore > currentBestScore) {
              currentBest = better;
              currentBestScore = betterScore;
            }
          } catch (TimeoutException e) {
            logger.log(Level.WARNING, "Sub-optimizer timed out.  Skipping.");
          } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Sub-optimizer threw an exception.  Skipping.", e.getCause());
          }
        }
      } catch (InterruptedException e) {
        logger.log(Level.WARNING, "Thread interrupted, returning current best.");
        break;
      }
    }
    return currentBest;
  }

  private Callable<T> runSingleThreadPass(final T initial, final double tempScale) {
    return new Callable<T>() {
      @Override
      public T call() {
        Optimizer<T> optimizer = optimizerFactory.create(new TemperatureFunction() {

          @Override
          public double temperature(int currentStep, int nSteps) {
            return tempScale * subTempFun.temperature(currentStep, nSteps);
          }
        });
        return optimizer.iterate(subOptimizerSteps, initial);
      }
    };
  }
}
