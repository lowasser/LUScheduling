package org.learningu.scheduling;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.commons.cli.ParseException;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Serial.SerialPeriod;
import org.learningu.scheduling.graph.Serial.SerialTeacher;
import org.learningu.scheduling.logic.ScheduleLogic;
import org.learningu.scheduling.logic.ScheduleLogicModule;
import org.learningu.scheduling.util.CommandLineModule;
import org.learningu.scheduling.util.Condition;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;

public class ScheduleLogicTest extends TestCase {

  static final Module SCHEDULE_MODULE = new AbstractModule() {

    @SuppressWarnings("unused")
    @Provides
    Schedule getSchedule(Program program, Table<ClassPeriod, Room, Section> startTable) {
      return ImmutableSchedule.create(program, startTable);
    }

    @Override
    protected void configure() {
      install(new ScheduleLogicModule());
      try {
        install(CommandLineModule.create(
            new String[0],
            ScheduleLogicModule.FLAGS_CLASSES.toArray(new Class[0])));
      } catch (ParseException e) {
        Throwables.propagate(e);
      }
    }
  };

  class SampleProgramModule extends TestProgramModule {
    @Override
    protected void configure() {
      super.configure();
      SerialPeriod tenAM = bindPeriod("10AM");
      SerialPeriod elevenAM = bindPeriod("11AM");
      SerialPeriod noon = bindPeriod("12PM");
      bindTimeBlock("Saturday Morning", tenAM, elevenAM, noon);
      SerialTeacher alice = bindTeacher("Alice", tenAM);
      SerialTeacher bob = bindTeacher("Bob", elevenAM);
      SerialTeacher carol = bindTeacher("Carol", tenAM, elevenAM);
      SerialTeacher dave = bindTeacher("Dave", tenAM, elevenAM);
      SerialTeacher ellie = bindTeacher("Ellie", tenAM, elevenAM, noon);
      bindRoom("Harper130", 75, elevenAM, noon);
      bindRoom("Harper135", 20, tenAM, elevenAM, noon);
      bindRoom("Harper141", 20, tenAM, noon);
      bindRoom("Harper142", 20, tenAM, elevenAM);
      bindCourse("ScienceCourse", 1, 1, 15, alice, carol);
      bindCourse("PiratesCourse", 1, 1, 40, bob);
      bindCourse("MathCourse", 2, 1, 10, carol);
      bindCourse("OrigamiCourse", 1, 2, 10, dave);
      bindCourse("ZombiesCourse", 1, 3, 15, ellie);
    }
  }

  public void assertSucceeds(Module... scheduleModules) {
    Injector injector = TestProgramModule.bindProgramObjects(new SampleProgramModule())
        .createChildInjector(scheduleModules);
    ScheduleLogic logic = injector.getInstance(ScheduleLogic.class);
    Schedule schedule = injector.getInstance(Schedule.class);
    Condition cond = Condition.create(Logger.getAnonymousLogger(), Level.FINE);
    logic.isValid(cond, schedule).assertPasses();
  }

  public void assertFails(Module... scheduleModules) {
    Injector injector = TestProgramModule.bindProgramObjects(new SampleProgramModule())
        .createChildInjector(scheduleModules);
    ScheduleLogic logic = injector.getInstance(ScheduleLogic.class);
    Schedule schedule = injector.getInstance(Schedule.class);
    Condition cond = Condition.create(Logger.getAnonymousLogger(), Level.FINE);
    assertFalse(logic.isValid(cond, schedule).passes());
  }

  public void testEmptySchedulePasses() {
    assertSucceeds(SCHEDULE_MODULE, new AbstractModule() {
      @Override
      protected void configure() {
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<ClassPeriod, Room, Section> scheduleTable() {
        return ImmutableTable.of();
      }
    });
  }

  public void testFullSchedulePasses() {
    assertSucceeds(SCHEDULE_MODULE, new AbstractModule() {
      @Override
      protected void configure() {
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<ClassPeriod, Room, Section> scheduleTable(
          @Named("10AM") ClassPeriod tenAM,
          @Named("11AM") ClassPeriod elevenAM,
          @Named("ScienceCourse") Course scienceCourse,
          @Named("PiratesCourse") Course piratesCourse,
          @Named("MathCourse") Course mathCourse,
          @Named("ZombiesCourse") Course zombiesCourse,
          @Named("Harper130") Room harper130,
          @Named("Harper135") Room harper135,
          @Named("Harper142") Room harper142) {
        ImmutableTable.Builder<ClassPeriod, Room, Section> builder = ImmutableTable.builder();
        builder.put(tenAM, harper142, scienceCourse.getSection(0));
        builder.put(elevenAM, harper142, mathCourse.getSection(0));
        builder.put(elevenAM, harper130, piratesCourse.getSection(0));
        builder.put(tenAM, harper135, zombiesCourse.getSection(0));
        return builder.build();
      }
    });
  }

  public void testTeacherUnavailableForPartOfCourseFails() {
    assertFails(SCHEDULE_MODULE, new AbstractModule() {
      @Override
      protected void configure() {
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<ClassPeriod, Room, Section> scheduleTable(
          @Named("11AM") ClassPeriod elevenAM,
          @Named("OrigamiCourse") Course origami,
          @Named("Harper135") Room harper135) {
        // Dave teaches a two-hour origami class, but is not available at noon.
        return ImmutableTable.of(elevenAM, harper135, origami.getSection(0));
      }
    });
  }

  public void testRoomUnavailableForPartOfCourseFails() {
    assertFails(SCHEDULE_MODULE, new AbstractModule() {
      @Override
      protected void configure() {
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<ClassPeriod, Room, Section> scheduleTable(
          @Named("10AM") ClassPeriod tenAM,
          @Named("ZombiesCourse") Course zombie,
          @Named("Harper141") Room harper141) {
        /*
         * Harper 141 is only available at 10 and noon (skipping 11) but the zombie class is 3
         * hours.
         */
        return ImmutableTable.of(tenAM, harper141, zombie.getSection(0));
      }
    });
  }

  public void testTeacherUnavailableFails() {
    assertFails(SCHEDULE_MODULE, new AbstractModule() {
      @Override
      protected void configure() {
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<ClassPeriod, Room, Section> scheduleTable(
          @Named("11AM") ClassPeriod elevenAM,
          @Named("ScienceCourse") Course scienceCourse,
          @Named("Harper135") Room harper135) {
        // Alice co-teaches science, but is unavailable at 11.
        ImmutableTable.Builder<ClassPeriod, Room, Section> builder = ImmutableTable.builder();
        builder.put(elevenAM, harper135, scienceCourse.getSection(0));
        return builder.build();
      }
    });
  }

  public void testTeacherConflictFails() {
    assertFails(SCHEDULE_MODULE, new AbstractModule() {
      @Override
      protected void configure() {
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<ClassPeriod, Room, Section> scheduleTable(
          @Named("10AM") ClassPeriod tenAM,
          @Named("ScienceCourse") Course scienceCourse,
          @Named("MathCourse") Course mathCourse,
          @Named("Harper135") Room harper135,
          @Named("Harper141") Room harper141) {
        // Carol teaches both science and math, but they are both scheduled for 10.
        ImmutableTable.Builder<ClassPeriod, Room, Section> builder = ImmutableTable.builder();
        builder.put(tenAM, harper135, scienceCourse.getSection(0));
        builder.put(tenAM, harper141, mathCourse.getSection(0));
        return builder.build();
      }
    });
  }

  public void testRoomUnavailableFails() {
    assertFails(SCHEDULE_MODULE, new AbstractModule() {
      @Override
      protected void configure() {
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<ClassPeriod, Room, Section> scheduleTable(
          @Named("11AM") ClassPeriod elevenAM,
          @Named("MathCourse") Course mathCourse,
          @Named("Harper141") Room harper141) {
        // Room 141 is not available at 11 am.
        return ImmutableTable.of(elevenAM, harper141, mathCourse.getSection(0));
      }
    });
  }

  public void testScheduleTwoSectionsSucceeds() {
    assertSucceeds(SCHEDULE_MODULE, new AbstractModule() {
      @Override
      protected void configure() {
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<ClassPeriod, Room, Section> scheduleTable(
          @Named("10AM") ClassPeriod tenAM,
          @Named("11AM") ClassPeriod elevenAM,
          @Named("MathCourse") Course mathCourse,
          @Named("Harper135") Room harper135) {
        // We doubly-scheduled math for both 10 and 11.
        ImmutableTable.Builder<ClassPeriod, Room, Section> builder = ImmutableTable.builder();
        builder.put(tenAM, harper135, mathCourse.getSection(0));
        builder.put(elevenAM, harper135, mathCourse.getSection(1));
        return builder.build();
      }
    });
  }

  public void testScheduleOneSectionTwiceFails() {
    assertFails(SCHEDULE_MODULE, new AbstractModule() {
      @Override
      protected void configure() {
      }

      @SuppressWarnings("unused")
      // provider
      @Provides
      Table<ClassPeriod, Room, Section> scheduleTable(
          @Named("10AM") ClassPeriod tenAM,
          @Named("11AM") ClassPeriod elevenAM,
          @Named("MathCourse") Course mathCourse,
          @Named("Harper135") Room harper135) {
        // We doubly-scheduled math for both 10 and 11.
        ImmutableTable.Builder<ClassPeriod, Room, Section> builder = ImmutableTable.builder();
        builder.put(tenAM, harper135, mathCourse.getSection(0));
        builder.put(elevenAM, harper135, mathCourse.getSection(0));
        return builder.build();
      }
    });
  }
}
