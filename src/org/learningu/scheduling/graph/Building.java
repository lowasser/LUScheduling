package org.learningu.scheduling.graph;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import org.learningu.scheduling.graph.SerialGraph.SerialBuilding;
import org.learningu.scheduling.graph.SerialGraph.SerialRoom;

public final class Building extends ProgramObject<SerialBuilding> {
  private final ImmutableList<Room> rooms;

  Building(Program program, SerialBuilding serial) {
    super(program, serial);
    ImmutableList.Builder<Room> roomsBuilder = ImmutableList.builder();
    for (SerialRoom room : serial.getRoomList()) {
      roomsBuilder.add(new Room(this, room));
    }
    this.rooms = roomsBuilder.build();
  }

  public ImmutableList<Room> getRooms() {
    return rooms;
  }

  @Override
  public int getId() {
    return serial.getBuildingId();
  }

  public String getName() {
    return serial.getName();
  }

  public static Function<SerialBuilding, Building> programWrapper(final Program program) {
    return new Function<SerialBuilding, Building>() {
      @Override
      public Building apply(SerialBuilding input) {
        return new Building(program, input);
      }
    };
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("name", getName()).toString();
  }
}
