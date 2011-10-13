package org.learningu.scheduling;

import static org.learningu.scheduling.TestingUtils.createRoom;
import static org.learningu.scheduling.TestingUtils.createTimeBlock;
import junit.framework.TestCase;

public class RoomTest extends TestCase {

  static final Serial.Program baseProgram;

  static {
    baseProgram = Serial.Program
        .newBuilder()
        .addTimeBlocks(createTimeBlock(0, "10-11"))
        .addTimeBlocks(createTimeBlock(1, "11-12"))
        .build();
  }

  public void testThrowsOnDuplicateRooms() {
    Serial.Room r1 = createRoom(0, "Harper 130", 0);
    Serial.Program p = Serial.Program.newBuilder(baseProgram).addRooms(r1).addRooms(r1).build();

    try {
      new Program(p);
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testCompatibleWithOneBlock() {
    Serial.Room r1 = createRoom(0, "Harper 130", 0);
    Serial.Program p = Serial.Program.newBuilder(baseProgram).addRooms(r1).build();
    Program program = new Program(p);

    Room h130 = program.getRoom(0);
    TimeBlock block0 = program.getTimeBlock(0);
    TimeBlock block1 = program.getTimeBlock(1);
    assertTrue(h130.isCompatibleWithTimeBlock(block0));
    assertFalse(h130.isCompatibleWithTimeBlock(block1));
  }

  public void testCompatibleWithNoBlocks() {
    Serial.Room r1 = createRoom(0, "Harper 130");
    Serial.Program p = Serial.Program.newBuilder(baseProgram).addRooms(r1).build();
    Program program = new Program(p);

    Room h130 = program.getRoom(0);
    TimeBlock block0 = program.getTimeBlock(0);
    TimeBlock block1 = program.getTimeBlock(1);
    assertFalse(h130.isCompatibleWithTimeBlock(block0));
    assertFalse(h130.isCompatibleWithTimeBlock(block1));
  }

  public void testThrowsOnDuplicateAvailableBlocks() {
    Serial.Room r1 = createRoom(0, "Harper 130", 0, 0);
    Serial.Program p = Serial.Program.newBuilder(baseProgram).addRooms(r1).build();

    try {
      new Program(p);
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testThrowsOnNoSuchTimeBlock() {
    Serial.Room r1 = createRoom(0, "Harper 130", 100);
    Serial.Program p = Serial.Program.newBuilder(baseProgram).addRooms(r1).build();

    try {
      new Program(p);
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
    }
  }
}
