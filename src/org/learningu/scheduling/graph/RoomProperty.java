package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import org.learningu.scheduling.graph.Serial.SerialRoomProperty;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;

/**
 * A property of a room that some courses may require. If the property is <i>binding</i>, no course
 * that doesn't require this property may use a room with this property.
 * 
 * @author lowasser
 */
public final class RoomProperty extends ProgramObject<SerialRoomProperty> {
  public static final Predicate<RoomProperty> IS_BINDING = new Predicate<RoomProperty>() {
    @Override
    public boolean apply(RoomProperty input) {
      return input.isBinding();
    }
  };

  private RoomProperty(Program program, SerialRoomProperty serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getPropertyId();
  }

  public String getDescription() {
    return serial.getDescription();
  }

  public boolean isBinding() {
    return serial.getIsBinding();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("description", serial.getDescription()).toString();
  }

  static Function<SerialRoomProperty, RoomProperty> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<SerialRoomProperty, RoomProperty>() {
      @Override
      public RoomProperty apply(SerialRoomProperty input) {
        return new RoomProperty(program, input);
      }
    };
  }

}
