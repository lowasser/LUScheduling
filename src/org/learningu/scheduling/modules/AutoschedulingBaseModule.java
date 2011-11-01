package org.learningu.scheduling.modules;

import org.learningu.scheduling.Autoscheduling;
import org.learningu.scheduling.flags.Flags;
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
    Flags.addFlagBindings(
        binder(),
        ProgramCacheFlags.class,
        LogicProvider.class,
        ConcurrentOptimizer.class,
        ExecutorServiceProvider.class,
        Autoscheduling.class,
        ScheduleValidator.class,
        LocalConflictLogic.class);
  }
}
