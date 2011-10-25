package org.learningu.scheduling.optimization;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.Period;
import org.learningu.scheduling.util.Flag;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public final class ConcurrentOptimizer<T> implements Optimizer<T> {
  private final Scorer<T> scorer;

  private final Provider<Optimizer<T>> optimizerProvider;

  private final int nThreads;

  private final ListeningExecutorService service;

  @Flag(
      value = "stepsPerOptimizerIteration",
      description = "Number of steps with which to run each optimizer thread per concurrent optimizer iteration",
      defaultValue = "1000")
  private final int stepsPerOptimizerIteration;

  @Flag(
      value = "iterationTimeout",
      description = "Maximum timeout for each concurrent optimizer iteration",
      defaultValue = "10s")
  private final Period timeout;

  private final Logger logger;

  @Inject
  ConcurrentOptimizer(Scorer<T> scorer, @SingleThread Provider<Optimizer<T>> optimizerProvider,
      @Named("nThreads") int nThreads, ListeningExecutorService service,
      @Named("stepsPerOptimizerIteration") int stepsPerOptimizerIteration,
      @Named("iterationTimeout") Period timeout, Logger logger) {
    this.scorer = scorer;
    this.optimizerProvider = optimizerProvider;
    this.nThreads = nThreads;
    this.service = service;
    this.stepsPerOptimizerIteration = stepsPerOptimizerIteration;
    this.timeout = timeout;
    this.logger = logger;
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
      logger.log(Level.INFO, "Concurrent optimizer, on iteration step {0}", step);
      logger.log(Level.FINEST, "Current best is {0}", currentBest);
      List<Callable<T>> independentThreads = Lists.newArrayListWithCapacity(nThreads);
      for (int i = 0; i < nThreads; i++) {
        independentThreads.add(runSingleThreadPass(currentBest));
      }
      try {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<ListenableFuture<T>> futures = (List) service.invokeAll(
            independentThreads,
            timeoutMillis,
            TimeUnit.MILLISECONDS);
        ListenableFuture<List<T>> successfulAsList = Futures.successfulAsList(futures);

        for (T better : successfulAsList.get()) {
          double betterScore = scorer.score(better);
          if (betterScore > currentBestScore) {
            currentBest = better;
            currentBestScore = betterScore;
          }
        }
      } catch (InterruptedException e) {
        Throwables.propagate(e);
      } catch (ExecutionException e) {
        Throwables.propagate(e);
      }
    }
    return currentBest;
  }

  private Callable<T> runSingleThreadPass(final T initial) {
    return new Callable<T>() {
      @Override
      public T call() {
        Optimizer<T> optimizer = optimizerProvider.get();
        return optimizer.iterate(stepsPerOptimizerIteration, initial);
      }
    };
  }
}
