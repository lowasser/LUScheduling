package org.learningu.scheduling.logic;

import org.learningu.scheduling.Schedule;
import org.learningu.scheduling.StartAssignment;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.util.ModifiedState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class TeacherConflictLogicTest extends BaseLogicTest {

  @Override
  protected Iterable<Module> modules() {
    return Iterables.concat(super.modules(), ImmutableList.of(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ScheduleLogic.class).to(TeacherConflictLogic.class);
      }
    }));
  }

  public void testOverlappingRoomConflict() {
    Schedule.Factory factory = injector.getInstance(Schedule.Factory.class);
    Schedule schedule = factory.create();
    Course science = getCourse("ScienceCourse");
    Course math = getCourse("MathCourse");
    ClassPeriod tenAM = getPeriod("10AM");
    Room harper141 = getRoom("Harper141");
    Room harper142 = getRoom("Harper142");
    // Carol teaches both science and math.
    ModifiedState<ScheduleValidator, Schedule> assign1 = schedule.assignStart(StartAssignment
        .create(tenAM, harper142, science.getSection(0)));
    assertTrue(assign1.getResult().isValid());
    schedule = assign1.getNewState();
    ModifiedState<ScheduleValidator, Schedule> assign2 = schedule.assignStart(StartAssignment
        .create(tenAM, harper141, math.getSection(0)));
    assertFalse(assign2.toString(), assign2.getResult().isValid());
    assertEquals(schedule.startAssignments(), assign2.getNewState().startAssignments());
  }
}
