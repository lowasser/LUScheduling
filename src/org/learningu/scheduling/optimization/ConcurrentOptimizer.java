package org.learningu.scheduling.optimization;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.Duration;
import org.learningu.scheduling.annotations.SingleThread;
import org.learningu.scheduling.flags.Converters;
import org.learningu.scheduling.flags.Flag;
import org.learningu.scheduling.pretty.Csv;

/**
 * A simulated annealing optimizer that operates by attempting several small optimizations in
 * parallel.
 * 
 * @author lowasser
 */
public final class ConcurrentOptimizer<T> implements Optimizer<T> {
  private final Scorer<T> scorer;

  private final OptimizerFactory<T> optimizerFactory;

  private final int nSubOptimizers;

  private final ExecutorService service;

  private final int subOptimizerSteps;

  @Inject(optional = true)
  @Flag(
      name = "noProgressCancel",
      optional = true,
      description = "If no additional progress is made in the specified time, stops optimizing.")
  private Duration noProgressCancel = Duration.standardSeconds(20);

  @Inject(optional = true)
  @Flag(
      name = "iterationTimeout",
      description = "Maximum timeout for each concurrent optimizer iteration",
      optional = true)
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
      List<Future<T>> futures = Lists.newArrayListWithCapacity(nSubOptimizers);
      ExecutorCompletionService<T> completionService = new ExecutorCompletionService<T>(service);
      double temp = primaryTempFun.temperature(step, steps);
      for (int i = 0; i < nSubOptimizers; i++) {
        futures.add(completionService.submit(runSingleThreadPass(currentBest, temp)));
      }
      try {
        for (int i = 0; i < nSubOptimizers; i++) {
          try {
            T better = completionService.poll(timeoutMillis, TimeUnit.MILLISECONDS).get();
            if (better == null) {
              continue;
            }
            double betterScore = scorer.score(better);
            if (betterScore > currentBestScore) {
              currentBest = better;
              currentBestScore = betterScore;
            }
          } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Sub-optimizer threw an exception.  Skipping.", e.getCause());
          }
        }
      } catch (InterruptedException e) {
        logger.log(Level.WARNING, "Thread interrupted, returning current best.");
        break;
      } finally {
        for (Future<T> future : futures) {
          future.cancel(true); // if it didn't finish in time, cancel
        }
      }
    }
    return currentBest;
  }

  public T iterate(Duration duration, T initial) {
    long timeoutMillis = iterTimeout.getMillis();
    long start = System.currentTimeMillis();
    long dur = duration.getMillis();
    long lastUpdate = start;
    int step;
    T currentBest = initial;
    double currentBestScore = scorer.score(initial);
    Csv.Builder builder = Csv.newBuilder();
    for (step = 0; System.currentTimeMillis() - start < dur; step++) {
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
              lastUpdate = System.currentTimeMillis();
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
      if (step % 20 == 0) {
        builder.add(Csv
            .newRowBuilder()
            .add("%d", System.currentTimeMillis() - start)
            .add("%8.3f", currentBestScore)
            .build());
      }
      if ((System.currentTimeMillis() - lastUpdate) > noProgressCancel.getMillis()) {
        logger.log(Level.INFO, "Cutting off optimization for lack of progress");
        break;
      }
    }
    if (step != 0) {
      logger.log(
          Level.INFO,
          "Average optimizer iteration took {0}",
          Duration
              .millis((long) (System.currentTimeMillis() - start) / step)
              .toPeriod()
              .toString(Converters.PERIOD_FORMATTER));
    }
    try {
      Files.write(builder.build().toString(), new File("optimization-log.csv"), Charsets.UTF_8);
    } catch (IOException e) {
      logger.throwing("ConcurrentOptimizer", "log", e);
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
