package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;

public final class TimeBlock extends ProgramObject<Serial.TimeBlock> {

  TimeBlock(Program program, org.learningu.scheduling.Serial.TimeBlock serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getBlockId();
  }

  static Function<Serial.TimeBlock, TimeBlock> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<Serial.TimeBlock, TimeBlock>() {
      @Override
      public TimeBlock apply(org.learningu.scheduling.Serial.TimeBlock input) {
        return new TimeBlock(program, input);
      }
    };
  }

}
