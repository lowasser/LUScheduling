package org.learningu.scheduling.optimization;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.learningu.scheduling.annotations.SingleThread;
import org.learningu.scheduling.pretty.Csv;

import edu.uchicago.lowasser.flaginjection.Converters;
import edu.uchicago.lowasser.flaginjection.Flag;

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

  private final ForkJoinPool pool;

  private final int subOptimizerSteps;

  @Inject(optional = true)
  @Flag(name = "noProgressCancel", optional = true,
      description = "If no additional progress is made in the specified time, stops optimizing.")
  private Duration noProgressCancel = Duration.standardMinutes(20);

  @Inject(optional = true)
  @Flag(name = "iterationTimeout",
      description = "Maximum timeout for each concurrent optimizer iteration", optional = true)
  private Duration iterTimeout = Duration.standardSeconds(10);

  private final Logger logger;

  private final TemperatureFunction primaryTempFun;

  private final TemperatureFunction subTempFun;

  private final Stopwatch stopwatch;

  @Inject
  ConcurrentOptimizer(
      Scorer<T> scorer,
      @SingleThread OptimizerFactory<T> optimizerProvider,
      @Named("primaryTempFun") TemperatureFunction primaryTempFun,
      @Named("subTempFun") TemperatureFunction subTempFun,
      @Named("nSubOptimizers") int nSubOptimizers,
      ForkJoinPool pool,
      @Named("subOptimizerSteps") int subOptimizerSteps,
      Logger logger,
      Stopwatch stopwatch) {
    this.scorer = scorer;
    this.optimizerFactory = optimizerProvider;
    this.nSubOptimizers = nSubOptimizers;
    this.pool = pool;
    this.stopwatch = stopwatch;
    this.subOptimizerSteps = subOptimizerSteps;
    this.logger = logger;
    this.primaryTempFun = primaryTempFun;
    this.subTempFun = subTempFun;
  }

  @Override
  public Scorer<T> getScorer() {
    return scorer;
  }

  @SuppressWarnings("serial")
  final class SingleOptimizationStep extends RecursiveTask<T> {
    final T initial;
    final double tempScale;

    private SingleOptimizationStep(T initial, double tempScale) {
      this.initial = initial;
      this.tempScale = tempScale;
    }

    @Override
    protected T compute() {
      Optimizer<T> optimizer = optimizerFactory.create(new TemperatureFunction() {

        @Override
        public double temperature(int currentStep, int nSteps) {
          return tempScale * subTempFun.temperature(currentStep, nSteps);
        }
      });
      return optimizer.iterate(subOptimizerSteps, initial);
    }
  }

  final class ParallelOptimizationStep extends RecursiveTask<T> {
    private final T initial;
    private final double temp;

    private ParallelOptimizationStep(T initial, double temp) {
      this.initial = initial;
      this.temp = temp;
    }

    @Override
    protected T compute() {
      List<ForkJoinTask<T>> passes = Lists.newArrayListWithCapacity(nSubOptimizers);
      for (int i = 0; i < nSubOptimizers; i++) {
        passes.add(new SingleOptimizationStep(initial, temp).fork());
      }
      T currentBest = initial;
      double currentBestScore = scorer.score(initial);
      for (ForkJoinTask<T> pass : passes) {
        T better = pass.join();
        double betterScore = scorer.score(better);
        if (betterScore > currentBestScore) {
          currentBest = better;
          currentBestScore = betterScore;
        }
      }
      return currentBest;
    }

  }

  @SuppressWarnings("serial")
  final class IterateStepsTask extends RecursiveTask<T> {
    private final T initial;
    private final int steps;

    private IterateStepsTask(T initial, int steps) {
      this.initial = initial;
      this.steps = steps;
    }

    @Override
    protected T compute() {
      T currentBest = initial;
      for (int step = 0; step < steps; step++) {
        logger.log(Level.INFO, "On iteration step {0}, current best has score {1}", new Object[] {
            step, scorer.score(currentBest) });
        double temp = primaryTempFun.temperature(step, steps);
        ForkJoinTask<T> task = new ParallelOptimizationStep(currentBest, temp);
        stopwatch.start();
        currentBest = task.invoke();
        stopwatch.stop();
        logger.log(Level.INFO, "Iteration step {0} took {1}ms wall clock time", new Object[] {
            step, stopwatch.elapsedMillis() });
      }
      return currentBest;
    }
  }

  @SuppressWarnings("serial")
  final class IterateTimeTask extends RecursiveTask<T> {
    private final T initial;
    private final Duration duration;

    private IterateTimeTask(T initial, Duration duration) {
      this.initial = initial;
      this.duration = duration;
    }

    @Override
    protected T compute() {
      long start = System.currentTimeMillis();
      Instant startInstant = Instant.now();
      long lastUpdate = start;
      int step;
      T currentBest = initial;
      Csv.Builder builder = Csv.newBuilder();
      long totalTime = 0;
      for (step = 0; new Duration(startInstant, Instant.now()).compareTo(duration) < 0; step++) {
        logger.log(
            Level.INFO,
            "On iteration step {0}, current best has score {1}; {2} has elapsed",
            new Object[] {
                step,
                scorer.score(currentBest),
                Converters.PERIOD_FORMATTER.print(new Duration(startInstant, Instant.now())
                    .toPeriod()) });
        double temp =
            primaryTempFun.temperature(
                (int) new Duration(startInstant, Instant.now()).getMillis(),
                (int) duration.getMillis());
        ForkJoinTask<T> task = new ParallelOptimizationStep(currentBest, temp);
        stopwatch.start();
        currentBest = task.invoke();
        stopwatch.stop();
        if (step % 20 == 0) {
          builder.add(Csv
              .newRowBuilder()
              .add("%d", System.currentTimeMillis() - start)
              .add("%8.3f", scorer.score(currentBest))
              .build());
        }
        logger.log(Level.FINE, "Iteration {0} took {1} of wall clock time", new Object[] { step,
            Converters.PERIOD_FORMATTER.print(Period.millis((int) stopwatch.elapsedMillis())) });
        totalTime += stopwatch.elapsedMillis();
        logger.log(
            Level.FINER,
            "Average step time: {0}",
            new Object[] { Converters.PERIOD_FORMATTER.print(Period
                .millis((int) (totalTime / (step + 1)))) });
        stopwatch.reset();
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

  }

  @Override
  public synchronized T iterate(int steps, T initial) {
    return pool.invoke(new IterateStepsTask(initial, steps));
  }

  public T iterate(Duration duration, T initial) {
    return pool.invoke(new IterateTimeTask(initial, duration));
  }
}
