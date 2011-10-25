package org.learningu.scheduling.schedule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.base.Objects;

/**
 * A representation of a section of a class being present in a room during some particular period.
 * 
 * @author lowasser
 */
public final class PresentAssignment implements Assignment {
  private final StartAssignment startAssignment;
  private final int index;

  PresentAssignment(StartAssignment startAssignment, int index) {
    this.startAssignment = checkNotNull(startAssignment);
    this.index = index;
    checkArgument(
        index >= 0 && index < getCourse().getPeriodLength(),
        "Course %s only has %s periods, but index is %s",
        getCourse(),
        getCourse().getPeriodLength(),
        index);
  }

  @Override
  public Room getRoom() {
    return startAssignment.getRoom();
  }

  @Override
  public Program getProgram() {
    return startAssignment.getProgram();
  }

  @Override
  public Section getCourse() {
    return startAssignment.getCourse();
  }

  public TimeBlock getTimeBlock() {
    return startAssignment.getTimeBlock();
  }

  @Override
  public StartAssignment getStartAssignment() {
    return startAssignment;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public ClassPeriod getPeriod() {
    return startAssignment.getPresentPeriods().get(index);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(startAssignment, index);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof PresentAssignment) {
      PresentAssignment other = (PresentAssignment) obj;
      return Objects.equal(startAssignment, other.startAssignment) && index == other.index;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return Objects
        .toStringHelper(this)
        .add("startAssignment", startAssignment)
        .add("index", index)
        .toString();
  }
}
