package org.learningu.scheduling.optimization;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.Period;
import org.learningu.scheduling.Flag;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public final class ConcurrentOptimizer<T> implements Optimizer<T> {
  private final Scorer<T> scorer;

  private final OptimizerFactory<T> optimizerFactory;

  @Flag(
      value = "nSubOptimizers",
      defaultValue = "1",
      description = "Number of independent sub-optimizers to use in each concurrent optimizer "
          + "iteration")
  private final int nSubOptimizers;

  private final ListeningExecutorService service;

  @Flag(
      value = "subOptimizerSteps",
      description = "Number of steps with which to run each sub-optimizer per concurrent optimizer"
          + " iteration",
      defaultValue = "1000")
  private final int subOptimizerSteps;

  @Flag(
      value = "iterationTimeout",
      description = "Maximum timeout for each concurrent optimizer iteration",
      defaultValue = "10s")
  private final Period timeout;

  private final Logger logger;

  private final TemperatureFunction primaryTempFun;

  private final TemperatureFunction subTempFun;

  @Inject
  ConcurrentOptimizer(
      Scorer<T> scorer,
      OptimizerFactory<T> optimizerProvider,
      TemperatureFunction primaryTempFun,
      @SingleThread TemperatureFunction subTempFun,
      @Named("nSubOptimizers") int nSubOptimizers,
      ListeningExecutorService service,
      @Named("subOptimizerSteps") int subOptimizerSteps,
      @Named("iterationTimeout") Period timeout,
      Logger logger) {
    this.scorer = scorer;
    this.optimizerFactory = optimizerProvider;
    this.nSubOptimizers = nSubOptimizers;
    this.service = service;
    this.subOptimizerSteps = subOptimizerSteps;
    this.timeout = timeout;
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
    long timeoutMillis = timeout.toStandardDuration().getMillis();
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
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<ListenableFuture<T>> futures = (List) service.invokeAll(independentThreads);

        for (ListenableFuture<T> future : futures) {
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
