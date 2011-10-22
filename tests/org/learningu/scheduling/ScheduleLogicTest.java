package org.learningu.scheduling;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
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

  public void testEmptySchedulePasses() {
    Injector injector = Guice.createInjector(new SampleProgramModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Schedule.class).to(DumbSchedule.class);
      }

      @SuppressWarnings("unused")
      @Provides
      public ScheduleLogic scheduleLogic(ScheduleLogicFlags flags, Logger logger) {
        logger.addHandler(new ConsoleHandler());
        logger.setLevel(Level.ALL);
        return new DefaultScheduleLogic(flags, logger);
      }

      @SuppressWarnings("unused")
      @Provides
      public Table<TimeBlock, Room, Course> emptyScheduleTable() {
        return ImmutableTable.of();
      }
    });

    ScheduleLogic logic = injector.getInstance(ScheduleLogic.class);
    Schedule schedule = injector.getInstance(Schedule.class);

    assertTrue(logic.isValid(schedule).passes());
  }

  public void testTeacherConflict() {
    Injector injector = Guice.createInjector(new SampleProgramModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Schedule.class).to(DumbSchedule.class);
      }

      @SuppressWarnings("unused")
      @Provides
      public ScheduleLogic scheduleLogic(ScheduleLogicFlags flags, Logger logger) {
        logger.addHandler(new ConsoleHandler());
        logger.setLevel(Level.ALL);
        return new DefaultScheduleLogic(flags, logger);
      }

      @SuppressWarnings("unused")
      @Provides
      public Table<TimeBlock, Room, Course> teacherConflictForAlice(
          @Named("Block0") TimeBlock block,
          @Named("Course0") Course aliceSmallCourse,
          @Named("Course2") Course aliceBigCourse,
          @Named("Harper130") Room harper130,
          @Named("Harper135") Room harper135) {
        ImmutableTable.Builder<TimeBlock, Room, Course> builder = ImmutableTable.builder();
        builder.put(block, harper135, aliceSmallCourse);
        builder.put(block, harper130, aliceBigCourse);
        return builder.build();
      }
    });

    ScheduleLogic logic = injector.getInstance(ScheduleLogic.class);
    Schedule schedule = injector.getInstance(Schedule.class);

    assertFalse(logic.isValid(schedule).passes());
  }

  public void testCourseConflict() {
    Injector injector = Guice.createInjector(new SampleProgramModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Schedule.class).to(DumbSchedule.class);
      }

      @SuppressWarnings("unused")
      @Provides
      public ScheduleLogic scheduleLogic(ScheduleLogicFlags flags, Logger logger) {
        logger.addHandler(new ConsoleHandler());
        logger.setLevel(Level.ALL);
        return new DefaultScheduleLogic(flags, logger);
      }

      @SuppressWarnings("unused")
      @Provides
      public Table<TimeBlock, Room, Course> courseScheduledTwice(
          @Named("Block0") TimeBlock block0,
          @Named("Block1") TimeBlock block1,
          @Named("Course0") Course course,
          @Named("Harper130") Room harper130,
          @Named("Harper135") Room harper135) {
        ImmutableTable.Builder<TimeBlock, Room, Course> builder = ImmutableTable.builder();
        builder.put(block0, harper135, course);
        builder.put(block1, harper130, course);
        return builder.build();
      }
    });

    ScheduleLogic logic = injector.getInstance(ScheduleLogic.class);
    Schedule schedule = injector.getInstance(Schedule.class);

    assertFalse(logic.isValid(schedule).passes());
  }
}
