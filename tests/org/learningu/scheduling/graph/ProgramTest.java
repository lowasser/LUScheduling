package org.learningu.scheduling.graph;

import java.util.Arrays;

import junit.framework.TestCase;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Serial.SerialCourse;
import org.learningu.scheduling.graph.Serial.SerialPeriod;
import org.learningu.scheduling.graph.Serial.SerialProgram;
import org.learningu.scheduling.graph.Serial.SerialRoom;
import org.learningu.scheduling.graph.Serial.SerialTeacher;
import org.learningu.scheduling.graph.Serial.SerialTimeBlock;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.primitives.Ints;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class ProgramTest extends TestCase {
  private SerialProgram serialProgram;

  @Override
  protected void setUp() throws Exception {
    SerialProgram.Builder programBuilder = SerialProgram.newBuilder();

    SerialTeacher alice = SerialTeacher.newBuilder()
        .setName("Alice")
        .setTeacherId(0)
        .addAvailablePeriods(0)
        .build();
    SerialTeacher bob = SerialTeacher.newBuilder()
        .setName("Bob")
        .setTeacherId(1)
        .addAvailablePeriods(1)
        .build();
    SerialTeacher carol = SerialTeacher.newBuilder()
        .setName("Carol")
        .setTeacherId(2)
        .addAllAvailablePeriods(Ints.asList(0, 1))
        .build();
    programBuilder.addAllTeachers(Arrays.asList(alice, carol, bob));

    SerialPeriod period0 = SerialPeriod.newBuilder().setPeriodId(0).setDescription("9AM").build();
    SerialPeriod period1 = SerialPeriod.newBuilder().setPeriodId(1).setDescription("10AM").build();
    SerialTimeBlock block0 = SerialTimeBlock.newBuilder()
        .setBlockId(0)
        .setDescription("Morning")
        .addAllPeriods(Arrays.asList(period0, period1))
        .build();
    programBuilder.addTimeBlocks(block0);

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
        .addAllAvailablePeriods(Ints.asList(0, 1))
        .build();
    SerialRoom harper135 = SerialRoom.newBuilder()
        .setRoomId(1)
        .setCapacity(15)
        .setName("Harper 135")
        .addAvailablePeriods(0)
        .build();
    programBuilder.addAllRooms(Arrays.asList(harper130, harper135));

    serialProgram = programBuilder.build();
  }

  public void testSerialization() {
    TestingUtils.assertMessageEquals(serialProgram, serialize(new Program(serialProgram)));
  }

  public void testByteStringSerialization() throws InvalidProtocolBufferException {
    ByteString bytes = serialProgram.toByteString();
    TestingUtils.assertMessageEquals(
        serialProgram,
        serialize(new Program(SerialProgram.parseFrom(bytes))));
  }

  SerialTeacher serialize(Teacher teacher) {
    SerialTeacher.Builder builder = SerialTeacher.newBuilder();
    builder.setTeacherId(teacher.getId());
    builder.setName(teacher.getName());
    for (ClassPeriod block : teacher.getProgram().compatiblePeriods(teacher)) {
      builder.addAvailablePeriods(block.getId());
    }
    return builder.build();
  }

  SerialCourse serialize(Course course) {
    SerialCourse.Builder builder = SerialCourse.newBuilder();
    builder.setCourseId(course.getId());
    builder.setCourseTitle(course.getTitle());
    builder.setEstimatedClassSize(course.getEstimatedClassSize());
    builder.setMaxClassSize(course.getMaxClassSize());
    for (Teacher teacher : course.getProgram().teachersForCourse(course)) {
      builder.addTeacherIds(teacher.getId());
    }
    return builder.build();
  }

  SerialPeriod serialize(ClassPeriod period) {
    SerialPeriod.Builder builder = SerialPeriod.newBuilder();
    builder.setPeriodId(period.getId());
    builder.setDescription(period.getDescription());
    return builder.build();
  }

  SerialTimeBlock serialize(TimeBlock block) {
    SerialTimeBlock.Builder builder = SerialTimeBlock.newBuilder();
    builder.setBlockId(block.getId());
    builder.setDescription(block.getDescription());
    for (ClassPeriod period : block.getPeriods()) {
      builder.addPeriods(serialize(period));
    }
    return builder.build();
  }

  SerialRoom serialize(Room room) {
    SerialRoom.Builder builder = SerialRoom.newBuilder();
    builder.setRoomId(room.getId());
    builder.setName(room.getName());
    builder.setCapacity(room.getCapacity());
    for (ClassPeriod period : room.getProgram().compatiblePeriods(room)) {
      builder.addAvailablePeriods(period.getId());
    }
    return builder.build();
  }

  SerialProgram serialize(Program program) {
    SerialProgram.Builder builder = SerialProgram.newBuilder();
    for (Teacher teacher : program.getTeachers()) {
      builder.addTeachers(serialize(teacher));
    }
    for (TimeBlock block : program.getTimeBlocks()) {
      builder.addTimeBlocks(serialize(block));
    }
    for (Course course : program.getCourses()) {
      builder.addCourses(serialize(course));
    }
    for (Room room : program.getRooms()) {
      builder.addRooms(serialize(room));
    }
    if (program.getName().length() > 0) {
      builder.setName(program.getName());
    }
    return builder.build();
  }
}
