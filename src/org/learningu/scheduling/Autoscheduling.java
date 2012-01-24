package org.learningu.scheduling;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.learningu.scheduling.flags.Flags;
import org.learningu.scheduling.schedule.Schedule;

@Singleton
public final class Autoscheduling {
  private final ListeningExecutorService service;

  @Inject
  Autoscheduling() {
    this.service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
  }

  public ListeningExecutorService getService() {
    return service;
  }

  public static void main(String[] args) throws IOException, InterruptedException,
      ExecutionException {
    /*
     * The overall control flow is as follows:
     * 
     * 1. {@code AutoschedulingBaseModule} announces to Guice all the classes with @Flag'd fields.
     * 
     * 2. {@code Flags.bootstrapFlagInjector} bootstraps this and the command-line arguments into a
     * module that properly injects flag values.
     * 
     * 3. We construct an instance of this class, {@code Autoscheduling}, whose primary role is
     * simply control flow. We build an {@code ExecutorService} with the specified number of
     * threads.
     * 
     * 4. {@code AutoschedulerDataSource} is constructed via injection, being primarily responsible
     * for reading in data from files that were specified on the command line.
     * 
     * 5. {@code AutoschedulerDataSource.buildModule()} does the I/O work of actually reading the
     * files, and constructs a module to provide its results wherever needed.
     * 
     * 6. {@code AutoschedulingConfigModule} provides a framework for translating protobuf
     * optimization specification into the actual optimizer, scorer, etc. objects. It injects
     * callbacks for use on the result of the optimization. In particular, {@code ScorerModule}
     * defines each of the scoring functions itself.
     * 
     * 7. {@code dataInjector}, built from all the above modules, constructs an {@code
     * Autoscheduler} object, which can be called to get the optimize schedule. The callable is
     * submitted to an executor service for execution.
     * 
     * 8. The callbacks are retrieved from the injector and added to the future result of the
     * optimization. This does any specified output with the finished schedule.
     * 
     * 9. The executor service is shut down gently, waiting for the callbacks to finish.
     */

    Logger logger = Logger.getLogger("Autoscheduling");
    logger.fine("Initializing injector with flags");
    Injector injector = Flags.bootstrapFlagInjector(args, new AutoschedulingBaseModule());
    logger.fine("Starting executor service");
    Autoscheduling auto = injector.getInstance(Autoscheduling.class);
    try {
      final ListeningExecutorService service = auto.getService();
      logger.fine("Injecting data source provider");
      AutoschedulerDataSource dataSource = injector.getInstance(AutoschedulerDataSource.class);
      logger.fine("Reading input files");
      Module dataModule = dataSource.buildModule();
      logger.fine("Bootstrapping into completely initialized injector");
      Injector dataInjector =
          injector.createChildInjector(
              dataModule,
              new AutoschedulingConfigModule(),
              new AbstractModule() {
                @Override
                protected void configure() {
                  bind(ExecutorService.class).to(ListeningExecutorService.class);
                  bind(ListeningExecutorService.class).toInstance(service);
                }
              });
      logger.fine("Initiating schedule optimization");
      ListenableFuture<Schedule> optimizedSchedule =
          service.submit(dataInjector.getInstance(Autoscheduler.class));
      logger.fine("Registering callbacks on result future");
      Set<FutureCallback<Schedule>> callbacks =
          dataInjector.getInstance(Key.get(new TypeLiteral<Set<FutureCallback<Schedule>>>() {}));
      final CountDownLatch latch = new CountDownLatch(callbacks.size());
      for (final FutureCallback<Schedule> callback : callbacks) {
        Futures.addCallback(optimizedSchedule, new FutureCallback<Schedule>() {
          @Override
          public void onSuccess(Schedule result) {
            callback.onSuccess(result);
            latch.countDown();
          }

          @Override
          public void onFailure(Throwable t) {
            callback.onFailure(t);
            latch.countDown();
          }
        }, service);
      }
      logger.fine("Waiting for registered tasks to complete");
      latch.await();
    } finally {
      auto.getService().shutdown();
    }
  }
}
