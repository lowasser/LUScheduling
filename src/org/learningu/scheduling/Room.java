package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

/**
 * A room at a LU program in which classes may be scheduled.
 * 
 * @author lowasser
 */
public final class Room extends ProgramObject<org.learningu.scheduling.Serial.Room> {

  public Room(Program program, org.learningu.scheduling.Serial.Room serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getRoomId();
  }

  public int getCapacity() {
    return serial.getCapacity();
  }

  private transient Set<TimeBlock> availableTimeBlocks;

  public Set<TimeBlock> getAvailableTimeBlocks() {
    Set<TimeBlock> result = availableTimeBlocks;
    if (result != null) {
      return result;
    }
    ImmutableSet.Builder<TimeBlock> builder = ImmutableSet.builder();
    for (int blockId : serial.getAvailableBlocksList()) {
      builder.add(program.getTimeBlock(blockId));
    }
    return availableTimeBlocks = builder.build();
  }

  static Function<Serial.Room, Room> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<Serial.Room, Room>() {
      @Override
      public Room apply(org.learningu.scheduling.Serial.Room input) {
        return new Room(program, input);
      }
    };
  }
}
