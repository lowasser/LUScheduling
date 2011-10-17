package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * A room at a LU program in which classes may be scheduled.
 * 
 * @author lowasser
 */
public final class Room extends ProgramObject<Serial.Room> {

  public Room(Program program, Serial.Room serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getRoomId();
  }

  public int getCapacity() {
    return serial.getCapacity();
  }

  public String getName() {
    return serial.getName();
  }

  // Does not cache!
  public Set<TimeBlock> getCompatibleTimeBlocks() {
    return ProgramObjectSet.create(Lists.transform(serial.getAvailableBlocksList(),
        program.getTimeBlocks().asLookupFunction()));
  }

  @Override
  public String toString() {
    return serial.hasName() ? getName() : super.toString();
  }

  static Function<Serial.Room, Room> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<Serial.Room, Room>() {
      @Override
      public Room apply(Serial.Room input) {
        return new Room(program, input);
      }
    };
  }
}
