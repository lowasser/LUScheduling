package org.learningu.scheduling;

import junit.framework.TestCase;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Serial.SerialTeacher;
import org.learningu.scheduling.graph.Serial.SerialTimeBlock;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;

public class ScheduleLogicTest extends TestCase {
  static final class DumbSchedule implements Schedule {
    final Program program;
    final Table<TimeBlock, Room, Course> table;

    @Inject
    DumbSchedule(Program program, Table<TimeBlock, Room, Course> table) {
      this.program = program;
      this.table = table;
    }

    @Override
    public Program getProgram() {
      return program;
    }

    @Override
    public Table<TimeBlock, Room, Course> getScheduleTable() {
      return table;
    }
  }

  class SampleProgramModule extends TestProgramModule {
    @Override
    protected void configure() {
      super.configure();
      SerialTimeBlock tenAM = bindTimeBlock("10AM");
      SerialTimeBlock elevenAM = bindTimeBlock("11AM");
      SerialTeacher alice = bindTeacher("Alice", tenAM);
      SerialTeacher bob = bindTeacher("Bob", elevenAM);
      SerialTeacher carol = bindTeacher("Carol", tenAM, elevenAM);
      bindRoom("Harper130", 75, elevenAM);
      bindRoom("Harper135", 20, tenAM, elevenAM);
      bindRoom("Harper141", 20, tenAM);
      bindCourse("ScienceCourse", 15, alice, carol);
      bindCourse("PiratesCourse", 40, bob);
      bindCourse("MathCourse", 10, carol);
    }
  }

  public void assertSucceeds(Module... scheduleModules) {
    Injector injector = TestProgramModule.bindProgramObjects(new SampleProgramModule())
        .createChildInjector(scheduleModules);
    ScheduleLogic logic = injector.getInstance(ScheduleLogic.class);
    Schedule schedule = injector.getInstance(Schedule.class);
    logic.isValid(schedule).assertPasses();
  }

  public void assertFails(Module... scheduleModules) {
    Injector injector = TestProgramModule.bindProgramObjects(new SampleProgramModule())
        .createChildInjector(scheduleModules);
    ScheduleLogic logic = injector.getInstance(ScheduleLogic.class);
    Schedule schedule = injector.getInstance(Schedule.class);
    assertFalse(logic.isValid(schedule).passes());
  }

  public void testEmptySchedulePasses() {
    assertSucceeds(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Schedule.class).to(DumbSchedule.class);
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<TimeBlock, Room, Course> scheduleTable() {
        return ImmutableTable.of();
      }
    });
  }

  public void testFullSchedulePasses() {
    assertSucceeds(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Schedule.class).to(DumbSchedule.class);
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<TimeBlock, Room, Course> scheduleTable(
          @Named("10AM") TimeBlock tenAM,
          @Named("11AM") TimeBlock elevenAM,
          @Named("ScienceCourse") Course scienceCourse,
          @Named("PiratesCourse") Course piratesCourse,
          @Named("MathCourse") Course mathCourse,
          @Named("Harper130") Room harper130,
          @Named("Harper135") Room harper135) {
        ImmutableTable.Builder<TimeBlock, Room, Course> builder = ImmutableTable.builder();
        builder.put(tenAM, harper135, scienceCourse);
        builder.put(elevenAM, harper135, mathCourse);
        builder.put(elevenAM, harper130, piratesCourse);
        return builder.build();
      }
    });
  }

  public void testTeacherUnavailableFails() {
    assertFails(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Schedule.class).to(DumbSchedule.class);
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<TimeBlock, Room, Course> scheduleTable(
          @Named("11AM") TimeBlock elevenAM,
          @Named("ScienceCourse") Course scienceCourse,
          @Named("Harper135") Room harper135) {
        // Alice co-teaches science, but is unavailable at 11.
        ImmutableTable.Builder<TimeBlock, Room, Course> builder = ImmutableTable.builder();
        builder.put(elevenAM, harper135, scienceCourse);
        return builder.build();
      }
    });
  }

  public void testTeacherConflictFails() {
    assertFails(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Schedule.class).to(DumbSchedule.class);
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<TimeBlock, Room, Course> scheduleTable(
          @Named("10AM") TimeBlock tenAM,
          @Named("ScienceCourse") Course scienceCourse,
          @Named("MathCourse") Course mathCourse,
          @Named("Harper135") Room harper135,
          @Named("Harper141") Room harper141) {
        // Carol teaches both science and math, but they are both scheduled for 10.
        ImmutableTable.Builder<TimeBlock, Room, Course> builder = ImmutableTable.builder();
        builder.put(tenAM, harper135, scienceCourse);
        builder.put(tenAM, harper141, mathCourse);
        return builder.build();
      }
    });
  }

  public void testRoomUnavailableFails() {
    assertFails(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Schedule.class).to(DumbSchedule.class);
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<TimeBlock, Room, Course> scheduleTable(
          @Named("11AM") TimeBlock elevenAM,
          @Named("MathCourse") Course mathCourse,
          @Named("Harper141") Room harper141) {
        // Room 141 is not available at 11 am.
        return ImmutableTable.of(elevenAM, harper141, mathCourse);
      }
    });
  }

  public void testDoublyScheduledClassFails() {
    assertFails(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Schedule.class).to(DumbSchedule.class);
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<TimeBlock, Room, Course> scheduleTable(
          @Named("10AM") TimeBlock tenAM,
          @Named("11AM") TimeBlock elevenAM,
          @Named("MathCourse") Course mathCourse,
          @Named("Harper135") Room harper135) {
        // We doubly-scheduled math for both 10 and 11.
        ImmutableTable.Builder<TimeBlock, Room, Course> builder = ImmutableTable.builder();
        builder.put(tenAM, harper135, mathCourse);
        builder.put(elevenAM, harper135, mathCourse);
        return builder.build();
      }
    });
  }
}
