package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.ProgramObject;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.inject.Inject;

/**
 * A default implementation of {@code ScheduleLogic}.
 * 
 * @author lowasser
 */
final class DefaultScheduleLogic implements ScheduleLogic {
  private final Program program;

  @Inject
  DefaultScheduleLogic(Program program) {
    this.program = program;
  }

  @Override
  public boolean isCompatible(Course course, TimeBlock block) {
    checkValid(course, block);
    return program.compatibleTimeBlocks(course).contains(block);
  }

  private void checkValid(ProgramObject<?> o1, ProgramObject<?> o2) {
    checkNotNull(o1);
    checkNotNull(o2);
    checkArgument(o1.getProgram() == o2.getProgram());
    checkArgument(program == o1.getProgram());
  }

  @Override
  public boolean isCompatible(Course course, Room room) {
    checkValid(course, room);
    return course.getMaxClassSize() <= room.getCapacity();
  }

  @Override
  public boolean isCompatible(Room room, TimeBlock block) {
    checkValid(room, block);
    return program.compatibleTimeBlocks(room).contains(block);
  }

  @Override
  public boolean isCompatible(Teacher teacher, TimeBlock block) {
    checkValid(teacher, block);
    return program.compatibleTimeBlocks(teacher).contains(block);
  }
}
