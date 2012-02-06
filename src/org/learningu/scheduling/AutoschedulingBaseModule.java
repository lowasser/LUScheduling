package org.learningu.scheduling;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import java.util.concurrent.ForkJoinPool;

import org.learningu.scheduling.flags.Flags;
import org.learningu.scheduling.graph.ProgramCacheFlags;
import org.learningu.scheduling.logic.LocalConflictLogic;
import org.learningu.scheduling.logic.ScheduleValidator;
import org.learningu.scheduling.optimization.ConcurrentOptimizer;

/**
 * The first module for autoscheduling, including bindings common to all runs. Specifically
 * includes bindings for all the classes with flagged fields or method arguments.
 * 
 * @author lowasser
 */
public final class AutoschedulingBaseModule extends AbstractModule {

  @Override
  protected void configure() {
    install(Flags.flagBindings(
        ProgramCacheFlags.class,
        ConcurrentOptimizer.class,
        Autoscheduling.class,
        ScheduleValidator.class,
        LocalConflictLogic.class,
        Autoscheduler.class,
        AutoschedulerDataSource.class,
        RoomPrettyPrinter.class,
        TeacherPrettyPrinter.class,
        ScheduleOutputCallback.class,
        CsvOutputCallback.class));
    bind(ForkJoinPool.class).asEagerSingleton();
  }

  @Provides
  @Named("main")
  String mainClass() {
    return "Autoscheduler";
  }
}
