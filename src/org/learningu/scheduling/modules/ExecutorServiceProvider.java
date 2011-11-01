package org.learningu.scheduling.modules;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.learningu.scheduling.flags.Flag;

import com.google.inject.Inject;
import com.google.inject.Provider;

final class ExecutorServiceProvider implements Provider<ExecutorService> {
  @Inject
  @Flag(
      name = "nThreads",
      description = "Number of threads to use in concurrent optimization")
  private int nThreads;

  @Override
  public ExecutorService get() {
    return Executors.newFixedThreadPool(nThreads);
  }
}