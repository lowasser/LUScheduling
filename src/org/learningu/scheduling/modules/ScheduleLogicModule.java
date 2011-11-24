package org.learningu.scheduling.modules;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;

import java.util.List;
import java.util.Map;

import org.learningu.scheduling.logic.ChainedScheduleLogic;
import org.learningu.scheduling.logic.DuplicateSectionLogic;
import org.learningu.scheduling.logic.GradeRangeLogic;
import org.learningu.scheduling.logic.LocalConflictLogic;
import org.learningu.scheduling.logic.PrerequisiteLogic;
import org.learningu.scheduling.logic.ResourceLogic;
import org.learningu.scheduling.logic.RoomConflictLogic;
import org.learningu.scheduling.logic.ScheduleLogic;
import org.learningu.scheduling.logic.SerialLogic.SerialLogicImpl;
import org.learningu.scheduling.logic.SerialLogic.SerialLogics;
import org.learningu.scheduling.logic.TeacherConflictLogic;

public class ScheduleLogicModule extends AbstractModule {

  @Override
  protected void configure() {
    MapBinder<SerialLogicImpl, ScheduleLogic> logicBindings = MapBinder.newMapBinder(
        binder(),
        SerialLogicImpl.class,
        ScheduleLogic.class);
    logicBindings.addBinding(SerialLogicImpl.DUPLICATE_SECTION).to(DuplicateSectionLogic.class);
    logicBindings.addBinding(SerialLogicImpl.LOCAL_CONFLICT).to(LocalConflictLogic.class);
    logicBindings.addBinding(SerialLogicImpl.ROOM_CONFLICT).to(RoomConflictLogic.class);
    logicBindings.addBinding(SerialLogicImpl.RESOURCE).to(ResourceLogic.class);
    logicBindings.addBinding(SerialLogicImpl.TEACHER_CONFLICT).to(TeacherConflictLogic.class);
    logicBindings.addBinding(SerialLogicImpl.PREREQUISITES).to(PrerequisiteLogic.class);
    logicBindings.addBinding(SerialLogicImpl.GRADE_RANGES).to(GradeRangeLogic.class);
  }

  @Provides
  ScheduleLogic logic(SerialLogics logics, Map<SerialLogicImpl, Provider<ScheduleLogic>> bindings) {
    List<ScheduleLogic> theLogics = Lists.newArrayList();
    for (SerialLogicImpl subLogic : logics.getLogicList()) {
      theLogics.add(bindings.get(subLogic).get());
    }
    return ChainedScheduleLogic.create(theLogics);
  }
}
