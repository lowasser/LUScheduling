package org.learningu.scheduling;

import static org.learningu.scheduling.TestingUtils.assertProgObjEquals;
import static org.learningu.scheduling.TestingUtils.createTeacher;
import static org.learningu.scheduling.TestingUtils.createTimeBlock;
import junit.framework.TestCase;

import com.google.common.collect.ImmutableSet;

public class CourseTest extends TestCase {

  static final Serial.Program baseProgram;

  static {
    baseProgram = Serial.Program
        .newBuilder()
        .addTimeBlocks(createTimeBlock(0, "10-11"))
        .addTimeBlocks(createTimeBlock(1, "11-12"))
        .build();
  }

  public void testThrowsOnDuplicateCourses() {
    Serial.Course c1 = Serial.Course
        .newBuilder()
        .setCourseId(0)
        .setCourseTitle("Pirates 101")
        .build();
    Serial.Program p = Serial.Program
        .newBuilder(baseProgram)
        .addCourses(c1)
        .addCourses(c1)
        .build();

    try {
      new Program(p);
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testRoomTooSmallCompatibility() {
    Serial.Course course1 = Serial.Course
        .newBuilder()
        .setCourseId(0)
        .setEstimatedClassSize(40)
        .setMaxClassSize(50)
        .build();
    Serial.Room h135 = Serial.Room
        .newBuilder()
        .setRoomId(0)
        .setCapacity(15)
        .setName("Harper 135")
        .build();
    Serial.Program p = Serial.Program
        .newBuilder(baseProgram)
        .addRooms(h135)
        .addCourses(course1)
        .build();

    Program program = new Program(p);
    assertFalse(program.getCourse(0).isCompatibleWithRoom(program.getRoom(0)));
  }

  public void testCourseTimeCompatibility() {
    Serial.Teacher t1 = createTeacher(0, "Alice", 1);
    Serial.Teacher t2 = createTeacher(1, "Blob", 0, 1);

    Serial.Course course1 = Serial.Course
        .newBuilder()
        .setCourseId(0)
        .addTeacherIds(0)
        .addTeacherIds(1)
        .build();

    Serial.Program p = Serial.Program
        .newBuilder(baseProgram)
        .addTeachers(t1)
        .addTeachers(t2)
        .addCourses(course1)
        .build();

    Program program = new Program(p);
    assertProgObjEquals(course1, program.getCourse(0));
    assertEquals(ImmutableSet.of(program.getTimeBlock(1)), program
        .getCourse(0)
        .getCompatibleTimeBlocks());
  }
}
