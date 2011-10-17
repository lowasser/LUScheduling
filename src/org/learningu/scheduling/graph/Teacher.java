package org.learningu.scheduling.graph;

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

  Teacher(Program program, Serial.Teacher serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getTeacherId();
  }

  // Does not cache!
  public Set<TimeBlock> getCompatibleTimeBlocks() {
    return ProgramObjectSet.create(Lists.transform(
        serial.getAvailableBlocksList(), program.getTimeBlocks().asLookupFunction()));
  }

  public String getName() {
    return serial.getName();
  }

  @Override
  public String toString() {
    return serial.hasName() ? serial.getName() : super.toString();
  }

  static Function<Serial.Teacher, Teacher> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<Serial.Teacher, Teacher>() {
      @Override
      public Teacher apply(Serial.Teacher input) {
        return new Teacher(program, input);
      }
    };
  }
}
