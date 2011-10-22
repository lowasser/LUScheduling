package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.learningu.scheduling.graph.Serial.SerialTeacher;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * A teacher at an LU program.
 * 
 * @author lowasser
 */
public final class Teacher extends ProgramObject<SerialTeacher> {

  Teacher(Program program, SerialTeacher serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getTeacherId();
  }

  // Does not cache!
  Set<ClassPeriod> getCompatiblePeriods() {
    return ProgramObjectSet.create(Lists.transform(
        serial.getAvailablePeriodsList(),
        program.periods.asLookupFunction()));
  }

  public String getName() {
    return serial.getName();
  }

  @Override
  public String toString() {
    return serial.hasName() ? serial.getName() : super.toString();
  }

  static Function<SerialTeacher, Teacher> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<SerialTeacher, Teacher>() {
      @Override
      public Teacher apply(SerialTeacher input) {
        return new Teacher(program, input);
      }
    };
  }
}
