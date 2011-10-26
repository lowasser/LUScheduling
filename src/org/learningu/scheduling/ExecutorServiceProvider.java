package org.learningu.scheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.learningu.scheduling.annotations.Flag;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

final class ExecutorServiceProvider implements Provider<ExecutorService> {
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
  public ExecutorService get() {
    return Executors.newFixedThreadPool(nThreads);
  }
}