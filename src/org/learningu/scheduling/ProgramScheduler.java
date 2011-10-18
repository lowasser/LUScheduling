package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.ProgramObject;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Responsible for the logic of scheduling an LU program.
 * 
 * @author lowasser
 */
public class ProgramScheduler {
  private final Cache<Course, Set<TimeBlock>> courseCompatibleBlocks;
  private final Cache<Room, Set<TimeBlock>> roomAvailableBlocks;
  private final Cache<Teacher, Set<TimeBlock>> teacherAvailableBlocks;
  private final Program program;

  public ProgramScheduler(final Program program) {
    this.program = checkNotNull(program);
    this.teacherAvailableBlocks = CacheBuilder
        .newBuilder()
        .softValues()
        .build(new CacheLoader<Teacher, Set<TimeBlock>>() {
          @Override
          public Set<TimeBlock> load(Teacher teacher) {
            return teacher.getCompatibleTimeBlocks();
          }
        });
    this.roomAvailableBlocks = CacheBuilder
        .newBuilder()
        .softValues()
        .build(new CacheLoader<Room, Set<TimeBlock>>() {
          @Override
          public Set<TimeBlock> load(Room room) {
            return room.getCompatibleTimeBlocks();
          }
        });
    this.courseCompatibleBlocks = CacheBuilder
        .newBuilder()
        .softValues()
        .build(new CacheLoader<Course, Set<TimeBlock>>() {
          @Override
          public Set<TimeBlock> load(Course course) {
            Set<TimeBlock> compatibleBlocks = Sets.newHashSet(program.getTimeBlocks());
            for (Teacher t : course.getTeachers()) {
              compatibleBlocks.retainAll(teacherAvailableBlocks.getUnchecked(t));
            }
            return ImmutableSet.copyOf(compatibleBlocks);
          }
        });
  }

  public Program getProgram() {
    return program;
  }

  public boolean isCompatible(Teacher teacher, TimeBlock block) {
    checkProgram(teacher);
    checkProgram(block);
    return teacherAvailableBlocks.getUnchecked(teacher).contains(block);
  }

  public boolean isCompatible(Course course, TimeBlock block) {
    checkProgram(course);
    checkProgram(block);
    return courseCompatibleBlocks.getUnchecked(course).contains(block);
  }

  public boolean isCompatible(Course course, Room room) {
    checkProgram(course);
    checkProgram(room);
    return room.getCapacity() >= course.getMaxClassSize();
  }

  public boolean isCompatible(Room room, TimeBlock block) {
    checkProgram(room);
    checkProgram(block);
    return roomAvailableBlocks.getUnchecked(room).contains(block);
  }

  private void checkProgram(ProgramObject<?> obj) {
    checkArgument(program == obj.getProgram());
  }
}
