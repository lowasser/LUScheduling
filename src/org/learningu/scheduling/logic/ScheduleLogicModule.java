package org.learningu.scheduling.logic;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * An injectable collection of flags for scheduling logic.
 * 
 * @author lowasser
 */
public final class ScheduleLogicModule extends AbstractModule {
  public static List<Class<?>> FLAGS_CLASSES = ImmutableList.<Class<?>> of(
      DuplicateSectionsLogic.class,
      LocalScheduleLogic.class,
      RoomOverlapScheduleLogic.class,
      TeacherConflictsScheduleLogic.class);

  @Override
  protected void configure() {
    bind(ScheduleLogic.class).to(CompositeScheduleLogic.class);
  }

  @Provides
  @CombinedLogics
  @Singleton
  Iterable<ScheduleLogic> combinedLogics(
      DuplicateSectionsLogic dupLogic,
      LocalScheduleLogic localLogic,
      RoomOverlapScheduleLogic overlapLogic,
      TeacherConflictsScheduleLogic teacherLogic) {
    return ImmutableList.of(dupLogic, localLogic, overlapLogic, teacherLogic);
  }
}