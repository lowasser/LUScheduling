package org.learningu.scheduling.logic;

import org.learningu.scheduling.MutableSchedule;
import org.learningu.scheduling.StartAssignment;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;

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
    MutableSchedule.Factory factory = injector.getInstance(MutableSchedule.Factory.class);
    Program program = injector.getInstance(Program.class);
    MutableSchedule schedule = factory.create(program);
    Course science = getCourse("ScienceCourse");
    Course math = getCourse("MathCourse");
    ClassPeriod tenAM = getPeriod("10AM");
    Room harper141 = getRoom("Harper141");
    Room harper142 = getRoom("Harper142");
    // Carol teaches both science and math.
    assertTrue(schedule.putAssignment(
        StartAssignment.create(tenAM, harper142, science.getSection(0))).isValid());
    ScheduleValidator putOverlapping = schedule.putAssignment(StartAssignment.create(
        tenAM,
        harper141,
        math.getSection(0)));
    assertFalse(putOverlapping.toString(), putOverlapping.isValid());
  }
}
