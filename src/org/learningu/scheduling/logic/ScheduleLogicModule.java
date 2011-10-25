package org.learningu.scheduling.logic;

import java.util.List;

import org.learningu.scheduling.Flag;
import org.learningu.scheduling.FlagsModule;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * Guice module for passing schedule logics to use at the command line.
 * 
 * @author lowasser
 */
public final class ScheduleLogicModule extends AbstractModule {
  public static final List<Class<?>> FLAG_CLASSES = ImmutableList.of(
      ImplChooser.class,
      LocalConflictLogic.class);

  enum LogicImpl {
    DUP_SECTIONS(DuplicateSectionLogic.class),
    TEACHER_CONFLICT(TeacherConflictLogic.class),
    LOCAL_CONFLICT(LocalConflictLogic.class),
    ROOM_CONFLICT(RoomConflictLogic.class),
    ROOM_PROPERTIES(RoomPropertyLogic.class);

    final Class<? extends ScheduleLogic> logicClass;

    private LogicImpl(Class<? extends ScheduleLogic> logicClass) {
      this.logicClass = logicClass;
    }
  }

  @Override
  protected void configure() {
    install(FlagsModule.create(ImplChooser.class));
    install(FlagsModule.create(LocalConflictLogic.class));
    bind(ScheduleLogic.class).toProvider(ImplChooser.class);
  }

  static final class ImplChooser implements Provider<ScheduleLogic> {
    @Flag(
        value = "scheduleLogics",
        multiple = true,
        defaultValue = "DUP_SECTIONS,TEACHER_CONFLICT,LOCAL_CONFLICT,ROOM_CONFLICT,ROOM_PROPERTIES",
        description = "Logics to use when validating schedules.  Options include DUP_SECTIONS, "
            + "TEACHER_CONFLICT, LOCAL_CONFLICT, ROOM_CONFLICT, ROOM_PROPERTIES. "
            + "The default is all of the above.")
    final List<LogicImpl> logics;

    final Injector injector;

    @Inject
    ImplChooser(@Named("scheduleLogics") List<LogicImpl> logics, Injector injector) {
      this.logics = logics;
      this.injector = injector;
    }

    @Override
    public ScheduleLogic get() {
      ImmutableList.Builder<ScheduleLogic> theLogics = ImmutableList.builder();
      for (LogicImpl logic : logics) {
        theLogics.add(injector.getInstance(logic.logicClass));
      }
      return ChainedScheduleLogic.create(theLogics.build());
    }
  }
}
