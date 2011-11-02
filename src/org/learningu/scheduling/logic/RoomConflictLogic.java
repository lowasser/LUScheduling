package org.learningu.scheduling.logic;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import java.util.List;

import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

/**
 * Logic for verifying that the assignment would not conflict with some other class.
 * 
 * @author lowasser
 */
public final class RoomConflictLogic extends ScheduleLogic {

  @Override
  public
      void
      validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
    super.validate(validator, schedule, assignment);
    validator.validateGlobal(
        assignment,
        schedule.occurringAt(assignment.getPeriod(), assignment.getRoom()).asSet(),
        "Classes may not use the same room at the same time");
  }

  private boolean overlaps(StartAssignment assign1, StartAssignment assign2) {
    Range<Integer> range1 = periodIndexRange(assign1);
    Range<Integer> range2 = periodIndexRange(assign2);
    return !assign1.getTimeBlock().equals(assign2.getTimeBlock()) || !range1.isConnected(range2)
        || !range1.intersection(range2).canonical(DiscreteDomains.integers()).isEmpty();
  }

  private static Range<Integer> periodIndexRange(StartAssignment assign) {
    int start = assign.getPeriod().getIndex();
    int length = assign.getSection().getPeriodLength();
    return Ranges.closedOpen(start, start + length);
  }

  private static <E> E getLast(List<E> list) {
    checkArgument(!list.isEmpty());
    return list.get(list.size() - 1);
  }
}
