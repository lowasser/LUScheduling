package org.learningu.scheduling;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

public class ProgramTest extends TestCase {
  public void testProgramWithTeachers() {
    Serial.Teacher t1 = Serial.Teacher.newBuilder().setTeacherId(1).setName("Testing Joe").build();
    Serial.Teacher t100 = Serial.Teacher
        .newBuilder()
        .setTeacherId(100)
        .setName("Testing Joe 100")
        .build();
    Serial.Program p = Serial.Program.newBuilder().addAllTeachers(Arrays.asList(t100, t1)).build();

    Program program = new Program(p);
    assertProgObjEquals(t1, program.getTeacher(1));
    assertProgObjEquals(t100, program.getTeacher(100));
  }

  public void testThrowsOnDupTeacherIds() {
    Serial.Teacher t1 = Serial.Teacher.newBuilder().setTeacherId(1).setName("Testing Joe").build();
    Serial.Teacher t100 = Serial.Teacher
        .newBuilder()
        .setTeacherId(1)
        .setName("Testing Joe 100")
        .build();
    Serial.Program p = Serial.Program.newBuilder().addAllTeachers(Arrays.asList(t100, t1)).build();

    try {
      new Program(p);
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testProgramWithTimeBlocks() {
    Serial.TimeBlock block1 = Serial.TimeBlock
        .newBuilder()
        .setBlockId(5)
        .setDescription("10-11AM")
        .build();
    Serial.TimeBlock block2 = Serial.TimeBlock
        .newBuilder()
        .setBlockId(6)
        .setDescription("11-12PM")
        .build();
    Serial.Program p = Serial.Program
        .newBuilder()
        .addAllTimeBlocks(Arrays.asList(block2, block1))
        .build();

    Program program = new Program(p);
    assertProgObjEquals(block1, program.getTimeBlock(5));
    assertProgObjEquals(block2, program.getTimeBlock(6));
    assertProgObjsInOrder(program.getTimeBlocks(), block1, block2);
  }

  public void testThrowsOnDupBlockIds() {
    Serial.TimeBlock block1 = Serial.TimeBlock
        .newBuilder()
        .setBlockId(5)
        .setDescription("10-11AM")
        .build();
    Serial.TimeBlock block2 = Serial.TimeBlock
        .newBuilder()
        .setBlockId(5)
        .setDescription("11-12PM")
        .build();
    Serial.Program p = Serial.Program
        .newBuilder()
        .addAllTimeBlocks(Arrays.asList(block2, block1))
        .build();

    try {
      new Program(p);
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
    }
  }

  <T extends Message> void assertProgObjEquals(T expected, ProgramObject<T> actual) {
    assertEquals(
        "Expected: " + TextFormat.printToString(expected) + "\nActual: "
            + TextFormat.printToString(actual.serial), expected.getAllFields(),
        actual.serial.getAllFields());
  }

  <T extends Message> void assertProgObjsInOrder(Collection<? extends ProgramObject<T>> actual,
      T... expected) {
    assertEquals(expected.length, actual.size());
    @SuppressWarnings("unchecked")
    ProgramObject<T>[] ts = (ProgramObject<T>[]) actual.toArray(new ProgramObject[0]);
    for (int i = 0; i < ts.length; i++) {
      assertProgObjEquals(expected[i], ts[i]);
    }
  }
}
