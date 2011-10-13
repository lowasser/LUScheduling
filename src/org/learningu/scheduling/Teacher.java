package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

/**
 * A teacher at an LU program.
 * 
 * @author lowasser
 */
public final class Teacher extends ProgramObject<Serial.Teacher> {

  Teacher(Program program, org.learningu.scheduling.Serial.Teacher serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getTeacherId();
  }

  static Function<Serial.Teacher, Teacher> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<Serial.Teacher, Teacher>() {
      @Override
      public Teacher apply(org.learningu.scheduling.Serial.Teacher input) {
        return new Teacher(program, input);
      }
    };
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

  public String getName() {
    return serial.getName();
  }
}
