package org.learningu.scheduling;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

public class TestingUtils {
  public static Serial.TimeBlock createTimeBlock(int id, String description) {
    return Serial.TimeBlock.newBuilder().setBlockId(id).setDescription(description).build();
  }

  public static Serial.Teacher createTeacher(int id, String name, int... availBlocks) {
    return Serial.Teacher
        .newBuilder()
        .setTeacherId(id)
        .setName(name)
        .addAllAvailableBlocks(Ints.asList(availBlocks))
        .build();
  }

  public static Serial.Room createRoom(int id, String name, int... availBlocks) {
    return Serial.Room
        .newBuilder()
        .setRoomId(id)
        .setName(name)
        .addAllAvailableBlocks(Ints.asList(availBlocks))
        .build();
  }

  static <T extends Message> void assertProgObjEquals(T expected, ProgramObject<T> actual) {
    assertEquals(
        "Expected: " + TextFormat.printToString(expected) + "\nActual: "
            + TextFormat.printToString(actual.serial), expected.getAllFields(),
        actual.serial.getAllFields());
  }

  static <T extends Message> void assertProgObjsInOrder(
      Collection<? extends ProgramObject<T>> actual, T... expected) {
    assertEquals(expected.length, actual.size());
    @SuppressWarnings("unchecked")
    ProgramObject<T>[] ts = (ProgramObject<T>[]) actual.toArray(new ProgramObject[0]);
    for (int i = 0; i < ts.length; i++) {
      assertProgObjEquals(expected[i], ts[i]);
    }
  }

  static <T extends Message> void assertProgObjsAnyOrder(
      Collection<? extends ProgramObject<T>> actual, T... expected) {
    Set<Map<FieldDescriptor, Object>> actualData = Sets.newHashSet();
    for (ProgramObject<T> t : actual) {
      actualData.add(t.serial.getAllFields());
    }
    assertEquals(expected.length, actual.size());
    for (T t : expected) {
      assertTrue(actualData.remove(t.getAllFields()));
    }
  }
}
