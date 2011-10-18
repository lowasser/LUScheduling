package org.learningu.scheduling.annealing;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * An optimizer that repeatedly has several sub-optimizers work independently and then come back
 * together to find the most successful point that any of them had come up with.
 * 
 * @author lowasser
 */
public final class ConcurrentOptimizer<T> implements Optimizer<T> {
  private final Scorer<T> scorer;
  private final ImmutableList<Optimizer<T>> optimizers;
  private final int nIterations;
  private final ExecutorService service;

  private T best;
  private double bestScore;

  @Inject
  ConcurrentOptimizer(Scorer<T> scorer, @Named("initial") T initial,
      OptimizerFactory<T> optFactory, @Named("nThreads") int nThreads,
      @Named("concurrentIterations") int nIterations, ExecutorService service) {
    this.scorer = checkNotNull(scorer);
    this.service = checkNotNull(service);
    ImmutableList.Builder<Optimizer<T>> builder = ImmutableList.builder();
    for (int i = 0; i < nThreads; i++) {
      builder.add(optFactory.createOptimizer(initial, scorer));
    }
    this.optimizers = builder.build();
    this.nIterations = nIterations;
  }

  @Override
  public Scorer<T> getScorer() {
    return scorer;
  }

  @Override
  public void iterate(int steps) {
    final int chunkSize = IntMath.divide(steps, nIterations, RoundingMode.CEILING);

    for (int i = 0; i < nIterations; i++) {
      List<Future<T>> futures = Lists.newArrayListWithCapacity(optimizers.size());
      for (final Optimizer<T> opt : optimizers) {
        futures.add(service.submit(new Callable<T>() {
          @Override
          public T call() {
            opt.iterate(chunkSize);
            return opt.getCurrentBest();
          }
        }));
      }

      for (Future<T> future : futures) {
        try {
          updateWithCandidate(future.get());
        } catch (ExecutionException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
          service.shutdown();
          return;
        }
      }

      for (Optimizer<T> opt : optimizers) {
        opt.updateWithCandidate(best);
      }
    }
  }

  @Override
  public T getCurrentBest() {
    return best;
  }

  @Override
  public synchronized boolean updateWithCandidate(T newCandidate) {
    double newCandidateScore = scorer.score(newCandidate);
    if (newCandidateScore > bestScore) {
      best = newCandidate;
      bestScore = newCandidateScore;
      return true;
    }
    return false;
  }

}
