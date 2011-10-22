package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.learningu.scheduling.graph.Serial.SerialPeriod;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

public final class ClassPeriod extends ProgramObject<SerialPeriod> {
  private final TimeBlock block;
  private final int index;

  ClassPeriod(SerialPeriod serial, TimeBlock block, int index) {
    super(block.getProgram(), serial);
    this.block = checkNotNull(block);
    this.index = index;
  }

  @Override
  public int getId() {
    return serial.getPeriodId();
  }

  public TimeBlock getTimeBlock() {
    return block;
  }

  public int getIndex() {
    return index;
  }

  public String getDescription() {
    return serial.getDescription();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("name", getDescription())
        .add("block", block)
        .add("index", index)
        .toString();
  }

  public Optional<ClassPeriod> getNextPeriod() {
    return getLaterPeriod(1);
  }

  public Optional<ClassPeriod> getLaterPeriod(int distance) {
    List<ClassPeriod> periods = block.getPeriods();
    if (index + distance < periods.size()) {
      return Optional.of(periods.get(index + distance));
    } else {
      return Optional.absent();
    }
  }

  public Optional<ClassPeriod> getPreviousPeriod() {
    return getEarlierPeriod(1);
  }

  public Optional<ClassPeriod> getEarlierPeriod(int distance) {
    List<ClassPeriod> periods = block.getPeriods();
    if (index >= distance) {
      return Optional.of(periods.get(index - distance));
    } else {
      return Optional.absent();
    }
  }

  /**
   * Returns a list of this period and all the periods in this block after it.
   */
  public List<ClassPeriod> getTailPeriods(boolean inclusive) {
    List<ClassPeriod> periods = block.getPeriods();
    return periods.subList(inclusive ? index : index + 1, periods.size());
  }

  /**
   * Returns a list of this period and all the periods in this block after it.
   */
  public List<ClassPeriod> getHeadPeriods(boolean inclusive) {
    List<ClassPeriod> periods = block.getPeriods();
    return periods.subList(0, inclusive ? index + 1 : index);
  }
}
