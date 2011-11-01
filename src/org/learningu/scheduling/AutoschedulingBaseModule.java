package org.learningu.scheduling;

import com.google.inject.AbstractModule;

import org.learningu.scheduling.flags.Flags;
import org.learningu.scheduling.graph.ProgramCacheFlags;
import org.learningu.scheduling.logic.LocalConflictLogic;
import org.learningu.scheduling.logic.LogicProvider;
import org.learningu.scheduling.logic.ScheduleValidator;
import org.learningu.scheduling.optimization.ConcurrentOptimizer;

/**
 * The first module for autoscheduling, including bindings common to all runs.
 * 
 * @author lowasser
 */
public final class AutoschedulingBaseModule extends AbstractModule {

  @Override
  protected void configure() {
    install(Flags.flagBindings(
        ProgramCacheFlags.class,
        LogicProvider.class,
        ConcurrentOptimizer.class,
        Autoscheduling.class,
        ScheduleValidator.class,
        LocalConflictLogic.class,
        Autoscheduler.class,
        AutoschedulerDataSource.class,
        RoomPrettyPrinter.class,
        TeacherPrettyPrinter.class,
        ScheduleOutputCallback.class));
  }
}
