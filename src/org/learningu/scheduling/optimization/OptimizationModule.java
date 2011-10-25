package org.learningu.scheduling.optimization;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.learningu.scheduling.util.Flag;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public final class OptimizationModule extends AbstractModule {
  public static final ImmutableList<Class<?>> FLAG_CLASSES = ImmutableList.of(
      ConcurrentOptimizer.class,
      ExecutorServiceProvider.class);

  @Override
  protected void configure() {
    bind(ExecutorService.class).to(ListeningExecutorService.class);
    bind(ListeningExecutorService.class)
        .toProvider(ExecutorServiceProvider.class)
        .asEagerSingleton();
  }

  static final class ExecutorServiceProvider implements Provider<ListeningExecutorService> {
    @Flag(
        value = "nThreads",
        defaultValue = "1",
        description = "Number of threads to use in concurrent optimization")
    private final int nThreads;

    @Inject
    ExecutorServiceProvider(@Named("nThreads") int nThreads) {
      this.nThreads = nThreads;
    }

    @Override
    public ListeningExecutorService get() {
      return MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(nThreads));
    }
  }

}
