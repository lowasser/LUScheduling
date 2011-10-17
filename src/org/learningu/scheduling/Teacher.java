package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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

  private transient Set<TimeBlock> compatibleTimeBlocks;

  public Set<TimeBlock> getCompatibleTimeBlocks() {
    Set<TimeBlock> result = compatibleTimeBlocks;
    if (result != null) {
      return result;
    }
    return compatibleTimeBlocks = ProgramObjectSet.create(Lists.transform(
        serial.getAvailableBlocksList(), program.getTimeBlocks().asLookupFunction()));
  }

  public String getName() {
    return serial.getName();
  }
}
