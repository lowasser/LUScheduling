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

public class RoomConflictLogicTest extends BaseLogicTest {

  @Override
  protected Iterable<Module> modules() {
    return Iterables.concat(super.modules(), ImmutableList.of(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ScheduleLogic.class).to(RoomConflictLogic.class);
      }
    }));
  }

  public void testOverlappingRoomConflict() {
    MutableSchedule.Factory factory = injector.getInstance(MutableSchedule.Factory.class);
    Program program = injector.getInstance(Program.class);
    MutableSchedule schedule = factory.create(program);
    Course origami = getCourse("OrigamiCourse");
    Course math = getCourse("MathCourse");
    ClassPeriod tenAM = getPeriod("10AM");
    ClassPeriod elevenAM = getPeriod("11AM");
    Room harper142 = getRoom("Harper142");
    // Origami is two hours.
    assertTrue(schedule.putAssignment(
        StartAssignment.create(tenAM, harper142, origami.getSection(0))).isValid());
    ScheduleValidator putOverlapping = schedule.putAssignment(StartAssignment.create(
        elevenAM,
        harper142,
        math.getSection(0)));
    assertFalse(putOverlapping.toString(), putOverlapping.isValid());
  }

  public void testOverlappingRoomConflictCommutative() {
    // Same test in a different order.
    MutableSchedule.Factory factory = injector.getInstance(MutableSchedule.Factory.class);
    Program program = injector.getInstance(Program.class);
    MutableSchedule schedule = factory.create(program);
    Course origami = getCourse("OrigamiCourse");
    Course math = getCourse("MathCourse");
    ClassPeriod tenAM = getPeriod("10AM");
    ClassPeriod elevenAM = getPeriod("11AM");
    Room harper142 = getRoom("Harper142");
    // Origami is two hours.
    assertTrue(schedule.putAssignment(
        StartAssignment.create(elevenAM, harper142, math.getSection(0))).isValid());
    ScheduleValidator putOverlapping = schedule.putAssignment(StartAssignment.create(
        tenAM,
        harper142,
        origami.getSection(0)));
    assertFalse(putOverlapping.toString(), putOverlapping.isValid());
  }
}