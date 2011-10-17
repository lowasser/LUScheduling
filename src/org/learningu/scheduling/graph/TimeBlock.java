package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;

/**
 * A block of time at an LU program, in which a single section of a single course might be
 * scheduled.
 * 
 * @author lowasser
 */
public final class TimeBlock extends ProgramObject<Serial.TimeBlock> {

  TimeBlock(Program program, Serial.TimeBlock serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getBlockId();
  }

  public String getDescription() {
    return serial.getDescription();
  }

  @Override
  public String toString() {
    if (serial.hasDescription()) {
      return getDescription();
    } else {
      return super.toString();
    }
  }

  static Function<Serial.TimeBlock, TimeBlock> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<Serial.TimeBlock, TimeBlock>() {
      @Override
      public TimeBlock apply(Serial.TimeBlock input) {
        return new TimeBlock(program, input);
      }
    };
  }

}
