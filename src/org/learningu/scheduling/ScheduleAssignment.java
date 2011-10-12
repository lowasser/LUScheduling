package org.learningu.scheduling;

public final class ScheduleAssignment {
  private final Program program;
  private final Room room;
  private final Course course;
  private final int section;
  private final TimeBlock block;

  ScheduleAssignment(Program program, org.learningu.scheduling.Serial.ScheduleAssignment serial) {
    this.program = program;
    this.room = program.getRoom(serial.getRoom());
    this.course = program.getCourse(serial.getCourse());
    this.section = serial.getSection();
    this.block = program.getTimeBlock(serial.getTimeBlock());
  }

  /**
   * Returns {@code true} if this assignment is valid on its own.
   */
  public boolean isLocallyValid() {
    
  }
  
  public boolean get
}
