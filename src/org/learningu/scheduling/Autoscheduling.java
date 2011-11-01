package org.learningu.scheduling;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.learningu.scheduling.flags.Flag;
import org.learningu.scheduling.flags.Flags;
import org.learningu.scheduling.schedule.Schedule;

@Singleton
public final class Autoscheduling {
  private final ListeningExecutorService service;

  @Inject
  Autoscheduling(@Flag(name = "nThreads") int nThreads) {
    this.service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(nThreads));
  }

  public ListeningExecutorService getService() {
    return service;
  }

  public static void main(String[] args) throws IOException, InterruptedException,
      ExecutionException {
    Logger logger = Logger.getLogger("Autoscheduling");
    logger.fine("Initializing injector with flags");
    Injector injector = Flags.bootstrapFlagInjector(args, new AutoschedulingBaseModule());
    logger.fine("Initializing data source reader");
    AutoschedulerDataSource dataSource = injector.getInstance(AutoschedulerDataSource.class);
    logger.fine("Constructing data source module");
    Module dataModule = dataSource.buildModule();
    Injector dataInjector = injector.createChildInjector(
        dataModule,
        new AutoschedulingConfigModule());
    Autoscheduling auto = dataInjector.getInstance(Autoscheduling.class);
    ListeningExecutorService service = auto.getService();
    ListenableFuture<Schedule> optimizedSchedule = service.submit(dataInjector
        .getInstance(Autoscheduler.class));
    Set<FutureCallback<Schedule>> callbacks = dataInjector.getInstance(Key
        .get(new TypeLiteral<Set<FutureCallback<Schedule>>>() {}));
    for (FutureCallback<Schedule> callback : callbacks) {
      Futures.addCallback(optimizedSchedule, callback);
    }
    optimizedSchedule.get();
    auto.getService().shutdown();
  }
}
