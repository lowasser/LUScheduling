package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.learningu.scheduling.graph.SerialGraph.SerialRoom;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

/**
 * A room at a LU program in which classes may be scheduled.
 * 
 * @author lowasser
 */
public final class Room extends ProgramObject<SerialRoom> implements Comparable<Room> {

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
  Set<ClassPeriod> getCompatiblePeriods() {
    return ImmutableSet.copyOf(Lists.transform(
        serial.getAvailablePeriodList(),
        Functions.forMap(program.periods)));
  }

  Set<RoomProperty> getRoomProperties() {
    return ImmutableSet.copyOf(Lists.transform(
        serial.getRoomPropertyList(),
        Functions.forMap(program.roomProperties)));
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

  @Override
  public int compareTo(Room o) {
    return Ints.compare(getId(), o.getId());
  }
}
