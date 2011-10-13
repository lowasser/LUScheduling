package org.learningu.scheduling;

import static org.learningu.scheduling.TestingUtils.createTeacher;
import static org.learningu.scheduling.TestingUtils.createTimeBlock;
import junit.framework.TestCase;

import com.google.common.collect.ImmutableSet;

public class TeacherTest extends TestCase {
  static final Serial.Program baseProgram;

  static {
    baseProgram = Serial.Program
        .newBuilder()
        .addTimeBlocks(createTimeBlock(0, "10-11"))
        .addTimeBlocks(createTimeBlock(1, "11-12"))
        .build();
  }

  public void testThrowsOnDuplicateTeachers() {
    Serial.Teacher t1 = createTeacher(0, "Alice", 0);
    Serial.Program p = Serial.Program
        .newBuilder(baseProgram)
        .addTeachers(t1)
        .addTeachers(t1)
        .build();

    try {
      new Program(p);
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testCompatibleWithOneBlock() {
    Serial.Teacher t1 = createTeacher(0, "Alice", 0);
    Serial.Program p = Serial.Program.newBuilder(baseProgram).addTeachers(t1).build();
    Program program = new Program(p);

    Teacher alice = program.getTeacher(0);
    TimeBlock block0 = program.getTimeBlock(0);
    TimeBlock block1 = program.getTimeBlock(1);
    assertEquals(ImmutableSet.of(block0), alice.getCompatibleTimeBlocks());
    assertTrue(alice.isCompatibleWithTimeBlock(block0));
    assertFalse(alice.isCompatibleWithTimeBlock(block1));
  }

  public void testCompatibleWithNoBlocks() {
    Serial.Teacher t1 = createTeacher(0, "Alice");
    Serial.Program p = Serial.Program.newBuilder(baseProgram).addTeachers(t1).build();
    Program program = new Program(p);

    Teacher alice = program.getTeacher(0);
    TimeBlock block0 = program.getTimeBlock(0);
    TimeBlock block1 = program.getTimeBlock(1);
    assertEquals(ImmutableSet.of(), alice.getCompatibleTimeBlocks());
    assertFalse(alice.isCompatibleWithTimeBlock(block0));
    assertFalse(alice.isCompatibleWithTimeBlock(block1));
  }

  public void testThrowsOnDuplicateAvailableBlocks() {
    Serial.Teacher t1 = createTeacher(0, "Alice", 0, 0);
    Serial.Program p = Serial.Program.newBuilder(baseProgram).addTeachers(t1).build();

    try {
      new Program(p);
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
    }
  }
}
