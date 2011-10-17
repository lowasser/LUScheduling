package org.learningu.scheduling;

import java.util.Arrays;

import junit.framework.TestCase;

import com.google.common.primitives.Ints;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class ProgramTest extends TestCase {
  private Serial.Program serialProgram;

  @Override
  protected void setUp() throws Exception {
    Serial.Program.Builder programBuilder = Serial.Program.newBuilder();

    Serial.Teacher alice = Serial.Teacher.newBuilder()
        .setName("Alice")
        .setTeacherId(0)
        .addAvailableBlocks(0)
        .build();
    Serial.Teacher bob = Serial.Teacher.newBuilder()
        .setName("Bob")
        .setTeacherId(1)
        .addAvailableBlocks(1)
        .build();
    Serial.Teacher carol = Serial.Teacher.newBuilder()
        .setName("Carol")
        .setTeacherId(2)
        .addAllAvailableBlocks(Ints.asList(0, 1))
        .build();
    programBuilder.addAllTeachers(Arrays.asList(alice, carol, bob));

    Serial.TimeBlock block0 = Serial.TimeBlock.newBuilder()
        .setBlockId(0)
        .setDescription("9-10")
        .build();
    Serial.TimeBlock block1 = Serial.TimeBlock.newBuilder()
        .setBlockId(1)
        .setDescription("10-11")
        .build();
    programBuilder.addAllTimeBlocks(Arrays.asList(block0, block1));

    Serial.Course course0 = Serial.Course.newBuilder()
        .setCourseId(0)
        .addTeacherIds(0)
        .addTeacherIds(2)
        .setEstimatedClassSize(8)
        .setMaxClassSize(10)
        .setCourseTitle("Maximum Science")
        .build();
    Serial.Course course1 = Serial.Course.newBuilder()
        .setCourseId(1)
        .addTeacherIds(1)
        .setEstimatedClassSize(30)
        .setMaxClassSize(40)
        .setCourseTitle("Pirates")
        .build();
    Serial.Course course2 = Serial.Course.newBuilder()
        .setCourseId(2)
        .addTeacherIds(0)
        .setEstimatedClassSize(15)
        .setMaxClassSize(20)
        .setCourseTitle("Pi-rates")
        .build();
    programBuilder.addAllCourses(Arrays.asList(course0, course2, course1));

    Serial.Room harper130 = Serial.Room.newBuilder()
        .setRoomId(0)
        .setCapacity(75)
        .setName("Harper 130")
        .addAllAvailableBlocks(Ints.asList(0, 1))
        .build();
    Serial.Room harper135 = Serial.Room.newBuilder()
        .setRoomId(1)
        .setCapacity(15)
        .setName("Harper 135")
        .addAllAvailableBlocks(Ints.asList(0))
        .build();
    programBuilder.addAllRooms(Arrays.asList(harper130, harper135));

    serialProgram = programBuilder.build();
  }

  public void testSerialization() {
    TestingUtils.assertMessageEquals(serialProgram, serialize(new Program(serialProgram)));
  }

  public void testByteStringSerialization() throws InvalidProtocolBufferException {
    ByteString bytes = serialProgram.toByteString();
    TestingUtils.assertMessageEquals(serialProgram,
        serialize(new Program(Serial.Program.parseFrom(bytes))));
  }

  Serial.Teacher serialize(Teacher teacher) {
    Serial.Teacher.Builder builder = Serial.Teacher.newBuilder();
    builder.setTeacherId(teacher.getId());
    builder.setName(teacher.getName());
    for (TimeBlock block : teacher.getCompatibleTimeBlocks()) {
      builder.addAvailableBlocks(block.getId());
    }
    return builder.build();
  }

  Serial.Course serialize(Course course) {
    Serial.Course.Builder builder = Serial.Course.newBuilder();
    builder.setCourseId(course.getId());
    builder.setCourseTitle(course.getTitle());
    builder.setEstimatedClassSize(course.getEstimatedClassSize());
    builder.setMaxClassSize(course.getMaxClassSize());
    for (Teacher teacher : course.getTeachers()) {
      builder.addTeacherIds(teacher.getId());
    }
    return builder.build();
  }

  Serial.TimeBlock serialize(TimeBlock block) {
    Serial.TimeBlock.Builder builder = Serial.TimeBlock.newBuilder();
    builder.setBlockId(block.getId());
    builder.setDescription(block.getDescription());
    return builder.build();
  }

  Serial.Room serialize(Room room) {
    Serial.Room.Builder builder = Serial.Room.newBuilder();
    builder.setRoomId(room.getId());
    builder.setName(room.getName());
    builder.setCapacity(room.getCapacity());
    for (TimeBlock block : room.getCompatibleTimeBlocks()) {
      builder.addAvailableBlocks(block.getId());
    }
    return builder.build();
  }

  Serial.Program serialize(Program program) {
    Serial.Program.Builder builder = Serial.Program.newBuilder();
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
