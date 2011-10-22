package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

public final class ImmutableSchedule extends Schedule {
  private final Program program;
  private final ImmutableTable<ClassPeriod, Room, Section> scheduleTable;
  private final ImmutableTable<ClassPeriod, Room, Section> startingTable;

  public static ImmutableSchedule copyOf(Schedule schedule) {
    checkNotNull(schedule);
    return new ImmutableSchedule(schedule.getProgram(),
        ImmutableTable.copyOf(schedule.getStartingTimeTable()),
        ImmutableTable.copyOf(schedule.getScheduleTable()));
  }

  static ImmutableSchedule create(Program program, Table<ClassPeriod, Room, Section> startingTable) {
    ImmutableTable.Builder<ClassPeriod, Room, Section> scheduleBuilder = ImmutableTable.builder();
    for (Cell<ClassPeriod, Room, Section> cell : startingTable.cellSet()) {
      Section section = cell.getValue();
      Room room = cell.getColumnKey();
      ClassPeriod startingPd = cell.getRowKey();
      Course course = section.getCourse();
      List<ClassPeriod> periods = startingPd.getTailPeriods(true).subList(
          0,
          course.getPeriodLength());
      assert periods.size() == course.getPeriodLength();
      for (ClassPeriod presentPeriod : periods) {
        scheduleBuilder.put(presentPeriod, room, section);
      }
    }
    return new ImmutableSchedule(program, scheduleBuilder.build(),
        ImmutableTable.copyOf(startingTable));
  }

  private ImmutableSchedule(
      Program program,
      ImmutableTable<ClassPeriod, Room, Section> scheduleTable,
      ImmutableTable<ClassPeriod, Room, Section> startingTable) {
    this.program = checkNotNull(program);
    this.scheduleTable = checkNotNull(scheduleTable);
    this.startingTable = checkNotNull(startingTable);
  }

  @Override
  public Program getProgram() {
    return program;
  }

  @Override
  public Table<ClassPeriod, Room, Section> getStartingTimeTable() {
    return startingTable;
  }

  @Override
  public Table<ClassPeriod, Room, Section> getScheduleTable() {
    return scheduleTable;
  }
}
