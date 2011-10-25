package org.learningu.scheduling.logic;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.learningu.scheduling.TestProgramModule;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Serial.SerialPeriod;
import org.learningu.scheduling.graph.Serial.SerialTeacher;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Names;

public abstract class BaseLogicTest extends TestCase {
  protected Injector injector;

  protected Iterable<Module> modules() {
    return ImmutableList.<Module> of(new TestProgramModule() {
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
        bindCourse("ScienceCourse", 1, 15, alice, carol);
        bindCourse("PiratesCourse", 1, 40, bob);
        bindCourse("MathCourse", 1, 10, carol);
        bindCourse("OrigamiCourse", 2, 10, dave);
        bindCourse("ZombiesCourse", 3, 15, ellie);
      }

      @SuppressWarnings("unused")
      @Provides
      ScheduleValidator validator(Logger logger) {
        return new ScheduleValidator(logger, Level.INFO, Level.WARNING);
      }
    });
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    injector = TestProgramModule.bindProgramObjects(Guice.createInjector(modules()));
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    injector = null;
  }

  protected Section getCourse(String string) {
    return injector.getInstance(Key.get(Section.class, Names.named(string)));
  }

  protected ClassPeriod getPeriod(String string) {
    return injector.getInstance(Key.get(ClassPeriod.class, Names.named(string)));
  }

  protected Room getRoom(String string) {
    return injector.getInstance(Key.get(Room.class, Names.named(string)));
  }

}
