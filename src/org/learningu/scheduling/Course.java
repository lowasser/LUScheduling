package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A course in an LU program.
 * 
 * @author lowasser
 */
public final class Course extends ProgramObject<Serial.Course> {
  /*
   * Features that might be added in the future include: prerequisites, multi-block classes.
   */

  Course(Program program, org.learningu.scheduling.Serial.Course serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getCourseId();
  }

  private transient Set<Teacher> teachers;

  public Set<Teacher> getTeachers() {
    Set<Teacher> result = teachers;
    if (result != null) {
      return result;
    }
    return teachers = ProgramObjectSet.create(Lists.transform(serial.getTeacherIdsList(), program
        .getTeachers()
        .asLookupFunction()));
  }

  public int getEstimatedClassSize() {
    return serial.getEstimatedClassSize();
  }

  public int getMaxClassSize() {
    return serial.getMaxClassSize();
  }

  public boolean isCompatibleWithRoom(Room room) {
    checkArgument(program.getRooms().contains(room));
    return getMaxClassSize() <= room.getCapacity();
  }

  public boolean isCompatibleWithTimeBlock(TimeBlock block) {
    checkArgument(program.getTimeBlocks().contains(block));
    return getCompatibleTimeBlocks().contains(block);
  }

  private transient Set<TimeBlock> compatibleTimeBlocks;

  public Set<TimeBlock> getCompatibleTimeBlocks() {
    Set<TimeBlock> result = compatibleTimeBlocks;
    if (result != null) {
      return result;
    }
    Set<TimeBlock> blocks = Sets.newHashSet(program.getTimeBlocks());
    for (Teacher t : getTeachers()) {
      blocks.retainAll(t.getCompatibleTimeBlocks());
    }
    return compatibleTimeBlocks = ImmutableSet.copyOf(blocks);
  }

  static Function<Serial.Course, Course> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<Serial.Course, Course>() {
      @Override
      public Course apply(org.learningu.scheduling.Serial.Course input) {
        return new Course(program, input);
      }
    };
  }
}
