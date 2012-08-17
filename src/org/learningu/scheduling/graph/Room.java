package org.learningu.scheduling.graph;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.Set;

import org.learningu.scheduling.graph.SerialGraph.SerialRoom;

/**
 * A room at a LU program in which classes may be scheduled.
 * 
 * @author lowasser
 */
public final class Room extends ProgramObject<SerialRoom> implements Comparable<Room> {

  private final Building building;
  
  Room(Building building, SerialRoom serial) {
    super(building.getProgram(), serial);
    this.building = building;
  }

  public Building getBuilding() {
    return building;
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

  Set<Resource> getResources() {
    return ImmutableSet.copyOf(Lists.transform(
        serial.getResourceList(),
        Functions.forMap(program.resources)));
  }

  @Override
  public String toString() {
    return serial.hasName() ? getName() : super.toString();
  }

  @Override
  public int compareTo(Room o) {
    return Ints.compare(getId(), o.getId());
  }

  @Override
  public String getShortDescription() {
    return getName();
  }
}
