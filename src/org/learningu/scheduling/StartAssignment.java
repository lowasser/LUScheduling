package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

import javax.annotation.Nullable;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.TimeBlock;

import com.google.common.base.Objects;

/**
 * A representation of the assignment of a specific section of a specific class to begin in a
 * specific room in a specific period.
 * 
 * @author lowasser
 */
public final class StartAssignment implements Assignment {
  public static StartAssignment create(ClassPeriod period, Room room, Section section) {
    return new StartAssignment(period, room, section);
  }

  private final ClassPeriod period;
  private final Room room;
  private final Section section;

  private StartAssignment(ClassPeriod period, Room room, Section section) {
    this.period = checkNotNull(period);
    this.room = checkNotNull(room);
    this.section = checkNotNull(section);
    checkArgument(period.getProgram() == room.getProgram()
        && period.getProgram() == section.getProgram());
    checkArgument(period.getIndex() + getCourse().getPeriodLength() <= getTimeBlock()
        .getPeriods()
        .size());
  }

  public List<ClassPeriod> getPresentPeriods() {
    return period.getTailPeriods(getCourse().getPeriodLength());
  }

  final class PresentAssignmentsList extends AbstractList<PresentAssignment> implements
      RandomAccess {
    @Override
    public PresentAssignment get(int index) {
      checkElementIndex(index, size());
      return new PresentAssignment(StartAssignment.this, index);
    }

    @Override
    public int size() {
      return getCourse().getPeriodLength();
    }
  }

  public PresentAssignment getPresentAssignment(int index) {
    return getPresentAssignments().get(index);
  }

  public List<PresentAssignment> getPresentAssignments() {
    return new PresentAssignmentsList();
  }

  @Override
  public Room getRoom() {
    return room;
  }

  @Override
  public Section getCourse() {
    return section;
  }

  @Override
  public Program getProgram() {
    return period.getProgram();
  }

  public TimeBlock getTimeBlock() {
    return period.getTimeBlock();
  }

  @Override
  public StartAssignment getStartAssignment() {
    return this;
  }

  @Override
  public ClassPeriod getPeriod() {
    return period;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(period, room, section);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof StartAssignment) {
      StartAssignment other = (StartAssignment) obj;
      return Objects.equal(period, other.period) && Objects.equal(room, other.room)
          && Objects.equal(section, other.section);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return Objects
        .toStringHelper(this)
        .add("period", period)
        .add("room", room)
        .add("section", section)
        .toString();
  }
}
