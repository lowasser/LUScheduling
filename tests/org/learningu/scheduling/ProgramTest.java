package org.learningu.scheduling;

import static org.learningu.scheduling.TestingUtils.assertProgObjEquals;
import static org.learningu.scheduling.TestingUtils.assertProgObjsAnyOrder;
import static org.learningu.scheduling.TestingUtils.assertProgObjsInOrder;
import static org.learningu.scheduling.TestingUtils.createTeacher;
import static org.learningu.scheduling.TestingUtils.createTimeBlock;

import java.util.Arrays;

import junit.framework.TestCase;

public class ProgramTest extends TestCase {
  public void testProgramWithTeachers() {
    Serial.Teacher t1 = createTeacher(1, "Joe");
    Serial.Teacher t100 = createTeacher(100, "Bill");
    Serial.Program p = Serial.Program.newBuilder().addAllTeachers(Arrays.asList(t100, t1)).build();

    Program program = new Program(p);
    assertProgObjsAnyOrder(program.getTeachers(), t1, t100);
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
    Serial.TimeBlock block1 = createTimeBlock(5, "10-11");
    Serial.TimeBlock block2 = createTimeBlock(6, "11-12");
    Serial.Program p = Serial.Program
        .newBuilder()
        .addAllTimeBlocks(Arrays.asList(block2, block1))
        .build();

    Program program = new Program(p);
    assertProgObjEquals(block1, program.getTimeBlock(5));
    assertProgObjEquals(block2, program.getTimeBlock(6));
    assertProgObjsAnyOrder(program.getTimeBlocks(), block1, block2);
  }

  public void testThrowsOnDupBlockIds() {
    Serial.TimeBlock block1 = createTimeBlock(5, "10-11");
    Serial.TimeBlock block2 = createTimeBlock(5, "11-12");
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
}
