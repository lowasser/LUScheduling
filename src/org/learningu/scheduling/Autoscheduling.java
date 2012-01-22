package org.learningu.scheduling;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.learningu.scheduling.flags.Flag;
import org.learningu.scheduling.flags.Flags;
import org.learningu.scheduling.schedule.Schedule;

@Singleton
public final class Autoscheduling {
  private final ListeningExecutorService service;

  @Inject
  Autoscheduling(@Flag(
      name = "nThreads",
      description = "Number of OS threads to use in the thread pool.") int nThreads) {
    this.service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(nThreads));
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
     * optimization.  This does any specified output with the finished schedule.
     * 
     * 9. The executor service is shut down gently, waiting for the callbacks to finish.
     */

    Logger logger = Logger.getLogger("Autoscheduling");
    logger.fine("Initializing injector with flags");
    Injector injector = Flags.bootstrapFlagInjector(args, new AutoschedulingBaseModule());
    Autoscheduling auto = injector.getInstance(Autoscheduling.class);
    try {
      final ListeningExecutorService service = auto.getService();
      logger.fine("Initializing data source reader");
      AutoschedulerDataSource dataSource = injector.getInstance(AutoschedulerDataSource.class);
      logger.fine("Constructing data source module");
      Module dataModule = dataSource.buildModule();
      Injector dataInjector = injector.createChildInjector(
          dataModule,
          new AutoschedulingConfigModule(),
          new AbstractModule() {
            @Override
            protected void configure() {
              bind(ExecutorService.class).to(ListeningExecutorService.class);
              bind(ListeningExecutorService.class).toInstance(service);
            }
          });
      ListenableFuture<Schedule> optimizedSchedule = service.submit(dataInjector
          .getInstance(Autoscheduler.class));
      Set<FutureCallback<Schedule>> callbacks = dataInjector.getInstance(Key
          .get(new TypeLiteral<Set<FutureCallback<Schedule>>>() {}));
      for (FutureCallback<Schedule> callback : callbacks) {
        Futures.addCallback(optimizedSchedule, callback, auto.getService());
      }
      optimizedSchedule.get();
    } finally {
      auto.getService().shutdown();
    }
  }
}
