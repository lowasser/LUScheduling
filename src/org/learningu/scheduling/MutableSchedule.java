package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

/**
 * A mutable implementation of a {@code Schedule}.
 * 
 * @author lowasser
 */
public final class MutableSchedule implements Schedule {
  public static MutableSchedule create(Program program) {
    return new MutableSchedule(program);
  }

  public static MutableSchedule create(Schedule schedule) {
    return new MutableSchedule(schedule);
  }

  private final Program program;
  private final Table<ClassPeriod, Room, Section> startingTimeTable;
  private final Table<ClassPeriod, Room, Section> scheduleTable;

  private MutableSchedule(Program program) {
    this.program = checkNotNull(program);
    this.startingTimeTable = ArrayTable.create(program.getPeriods(), program.getRooms());
    this.scheduleTable = ArrayTable.create(program.getPeriods(), program.getRooms());
  }

  private MutableSchedule(Schedule schedule) {
    this(schedule.getProgram());
    startingTimeTable.putAll(schedule.getStartingTimeTable());
    scheduleTable.putAll(schedule.getScheduleTable());
  }

  @Override
  public Program getProgram() {
    return program;
  }

  @Override
  public Table<ClassPeriod, Room, Section> getStartingTimeTable() {
    return startingTimeTable;
  }

  @Override
  public Table<ClassPeriod, Room, Section> getScheduleTable() {
    return scheduleTable;
  }

  /**
   * Clears out the room at the specified time, deleting that entire section if necessary.
   * 
   * Returns the removed section.
   */
  public Section removeAt(ClassPeriod period, Room room) {
    Section section = scheduleTable.get(period, room);
    if (section == null) {
      return null;
    } else {
      startingTimeTable.column(room).values().remove(section);
      scheduleTable.column(room).values().removeAll(ImmutableSet.of(section));
      return section;
    }
  }

  /**
   * Forces the specified class to start at the specified time, removing any classes that would
   * conflict with that room.
   * 
   * Returns {@code true} upon success, or {@code false} if there were not enough periods left in
   * the block to schedule the class.
   */
  public boolean forcePut(ClassPeriod startingPeriod, Room room, Section section) {
    Course course = section.getCourse();
    int periods = course.getPeriodLength();

    List<ClassPeriod> necessary = startingPeriod.getTailPeriods(true);
    if (necessary.size() < periods) {
      return false;
    }
    necessary = necessary.subList(0, periods);
    for (ClassPeriod pd : necessary) {
      removeAt(pd, room);
      scheduleTable.put(pd, room, section);
    }
    startingTimeTable.put(startingPeriod, room, section);
    return true;
  }

  /**
   * Start the specified class in the specified room at the specified time. If this would directly
   * conflict with a preexisting class, fails.
   */
  public boolean put(ClassPeriod startingPeriod, Room room, Section section) {
    Course course = section.getCourse();
    int periods = course.getPeriodLength();

    List<ClassPeriod> necessary = startingPeriod.getTailPeriods(true);
    if (necessary.size() < periods) {
      return false;
    }
    necessary = necessary.subList(0, periods);
    for (ClassPeriod pd : necessary) {
      if (scheduleTable.contains(pd, room)) {
        return false;
      }
    }

    startingTimeTable.put(startingPeriod, room, section);
    for (ClassPeriod pd : necessary) {
      scheduleTable.put(pd, room, section);
    }
    return true;
  }

}
