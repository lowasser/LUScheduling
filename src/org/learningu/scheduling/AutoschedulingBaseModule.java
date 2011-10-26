package org.learningu.scheduling;

import org.learningu.scheduling.graph.ProgramCacheFlags;
import org.learningu.scheduling.logic.LocalConflictLogic;
import org.learningu.scheduling.logic.LogicProvider;
import org.learningu.scheduling.logic.ScheduleValidator;
import org.learningu.scheduling.optimization.ConcurrentOptimizer;

import com.google.inject.AbstractModule;

/**
 * The first module for autoscheduling, including bindings common to all runs.
 * 
 * @author lowasser
 */
public final class AutoschedulingBaseModule extends AbstractModule {

  @Override
  protected void configure() {
    install(FlagsModule.create(ProgramCacheFlags.class));
    install(FlagsModule.create(LogicProvider.class));
    install(FlagsModule.create(ConcurrentOptimizer.class));
    install(FlagsModule.create(ExecutorServiceProvider.class));
    install(FlagsModule.create(Autoscheduling.class));
    install(FlagsModule.create(ScheduleValidator.class));
    install(FlagsModule.create(LocalConflictLogic.class));
  }
}
