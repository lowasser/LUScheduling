package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.learningu.scheduling.graph.Serial.SerialRoom;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * A room at a LU program in which classes may be scheduled.
 * 
 * @author lowasser
 */
public final class Room extends ProgramObject<SerialRoom> {

  public Room(Program program, SerialRoom serial) {
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
  Set<TimeBlock> getCompatibleTimeBlocks() {
    return ProgramObjectSet.create(Lists.transform(serial.getAvailableBlocksList(),
        program.timeBlocks.asLookupFunction()));
  }

  @Override
  public String toString() {
    return serial.hasName() ? getName() : super.toString();
  }

  static Function<SerialRoom, Room> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<SerialRoom, Room>() {
      @Override
      public Room apply(SerialRoom input) {
        return new Room(program, input);
      }
    };
  }
}
