package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

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

  private transient Set<TimeBlock> compatibleTimeBlocks;

  public Set<TimeBlock> getCompatibleTimeBlocks() {
    Set<TimeBlock> result = compatibleTimeBlocks;
    if (result != null) {
      return result;
    }
    return compatibleTimeBlocks = ProgramObjectSet.create(Lists.transform(
        serial.getAvailableBlocksList(), program.getTimeBlocks().asLookupFunction()));
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
