package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * A single assignment on a schedule, of a course to a time and room.
 * 
 * @author lowasser
 */
public final class ScheduleAssignment {
  public static ScheduleAssignment createAssignment(Course course, TimeBlock timeBlock, Room room) {
    return new ScheduleAssignment(course, timeBlock, room);
  }

  private final Program program;
  private final Course course;
  private final TimeBlock timeBlock;
  private final Room room;
  private transient Serial.ScheduleAssignment serial;

  private ScheduleAssignment(Program program,
      org.learningu.scheduling.Serial.ScheduleAssignment serial) {
    this.program = checkNotNull(program);
    this.serial = checkNotNull(serial);
    this.course = program.getCourse(serial.getCourseId());
    this.timeBlock = program.getTimeBlock(serial.getTimeBlockId());
    this.room = program.getRoom(serial.getRoomId());
  }

  private ScheduleAssignment(Course course, TimeBlock timeBlock, Room room) {
    this.course = checkNotNull(course);
    this.timeBlock = checkNotNull(timeBlock);
    this.room = checkNotNull(room);
    this.program = course.program;
    checkArgument(program == timeBlock.program && program == room.program);
  }

  public boolean isCompatible() {
    return course.isCompatibleWithRoom(room) && course.isCompatibleWithTimeBlock(timeBlock);
  }

  public Course getCourse() {
    return course;
  }

  public TimeBlock getTimeBlock() {
    return timeBlock;
  }

  public Room getRoom() {
    return room;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(course, timeBlock, room);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof ScheduleAssignment) {
      ScheduleAssignment assign = (ScheduleAssignment) obj;
      return Objects.equal(course, assign.getCourse())
          && Objects.equal(timeBlock, assign.getTimeBlock())
          && Objects.equal(room, assign.getRoom());
    }
    return false;
  }

  @Override
  public String toString() {
    return Objects
        .toStringHelper(this)
        .add("course", course)
        .add("timeBlock", timeBlock)
        .add("room", room)
        .toString();
  }

  public Serial.ScheduleAssignment getSerial() {
    if (serial != null) {
      return serial;
    } else {
      return serial = Serial.ScheduleAssignment
          .newBuilder()
          .setRoomId(room.getId())
          .setCourseId(course.getId())
          .setTimeBlockId(timeBlock.getId())
          .build();
    }
  }
}
