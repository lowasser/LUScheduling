package org.learningu.scheduling;

import java.util.Arrays;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.ProgramCacheFlags;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Serial.SerialCourse;
import org.learningu.scheduling.graph.Serial.SerialProgram;
import org.learningu.scheduling.graph.Serial.SerialRoom;
import org.learningu.scheduling.graph.Serial.SerialTeacher;
import org.learningu.scheduling.graph.Serial.SerialTimeBlock;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.primitives.Ints;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

public class SampleProgramModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ScheduleLogicFlags.class).toInstance(ScheduleLogicFlags.DEFAULTS);
    bind(ProgramCacheFlags.class).toInstance(ProgramCacheFlags.DEFAULTS);
  }

  @Provides
  @Named("Alice")
  Teacher alice(Program program) {
    return program.getTeacher(0);
  }

  @Provides
  @Named("Bob")
  Teacher bob(Program program) {
    return program.getTeacher(1);
  }

  @Provides
  @Named("Carol")
  Teacher carol(Program program) {
    return program.getTeacher(2);
  }

  @Provides
  @Named("Block0")
  TimeBlock block0(Program program) {
    return program.getTimeBlock(0);
  }

  @Provides
  @Named("Block1")
  TimeBlock block1(Program program) {
    return program.getTimeBlock(1);
  }

  // Taught by Alice and Carol.
  @Provides
  @Named("Course0")
  Course course0(Program program) {
    return program.getCourse(0);
  }

  // Taught by Bob.
  @Provides
  @Named("Course1")
  Course course1(Program program) {
    return program.getCourse(1);
  }

  // Taught by Alice.
  @Provides
  @Named("Course2")
  Course course2(Program program) {
    return program.getCourse(2);
  }

  @Provides
  @Named("Harper130")
  Room harper130(Program program) {
    return program.getRoom(0);
  }

  @Provides
  @Named("Harper135")
  Room harper135(Program program) {
    return program.getRoom(1);
  }

  @Provides
  SerialProgram createSampleSerialProgram() {
    SerialProgram.Builder programBuilder = SerialProgram.newBuilder();

    SerialTeacher alice = SerialTeacher.newBuilder()
        .setName("Alice")
        .setTeacherId(0)
        .addAvailableBlocks(0)
        .build();
    SerialTeacher bob = SerialTeacher.newBuilder()
        .setName("Bob")
        .setTeacherId(1)
        .addAvailableBlocks(1)
        .build();
    SerialTeacher carol = SerialTeacher.newBuilder()
        .setName("Carol")
        .setTeacherId(2)
        .addAllAvailableBlocks(Ints.asList(0, 1))
        .build();
    programBuilder.addAllTeachers(Arrays.asList(alice, carol, bob));

    SerialTimeBlock block0 = SerialTimeBlock.newBuilder()
        .setBlockId(0)
        .setDescription("9-10")
        .build();
    SerialTimeBlock block1 = SerialTimeBlock.newBuilder()
        .setBlockId(1)
        .setDescription("10-11")
        .build();
    programBuilder.addAllTimeBlocks(Arrays.asList(block0, block1));

    SerialCourse course0 = SerialCourse.newBuilder()
        .setCourseId(0)
        .addTeacherIds(0)
        .addTeacherIds(2)
        .setEstimatedClassSize(8)
        .setMaxClassSize(10)
        .setCourseTitle("Maximum Science")
        .build();
    SerialCourse course1 = SerialCourse.newBuilder()
        .setCourseId(1)
        .addTeacherIds(1)
        .setEstimatedClassSize(30)
        .setMaxClassSize(40)
        .setCourseTitle("Pirates")
        .build();
    SerialCourse course2 = SerialCourse.newBuilder()
        .setCourseId(2)
        .addTeacherIds(0)
        .setEstimatedClassSize(15)
        .setMaxClassSize(20)
        .setCourseTitle("Pi-rates")
        .build();
    programBuilder.addAllCourses(Arrays.asList(course0, course2, course1));

    SerialRoom harper130 = SerialRoom.newBuilder()
        .setRoomId(0)
        .setCapacity(75)
        .setName("Harper 130")
        .addAllAvailableBlocks(Ints.asList(0, 1))
        .build();
    SerialRoom harper135 = SerialRoom.newBuilder()
        .setRoomId(1)
        .setCapacity(15)
        .setName("Harper 135")
        .addAllAvailableBlocks(Ints.asList(0))
        .build();
    programBuilder.addAllRooms(Arrays.asList(harper130, harper135));

    return programBuilder.build();
  }
}
