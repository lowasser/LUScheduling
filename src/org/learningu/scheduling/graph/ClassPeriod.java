package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.learningu.scheduling.graph.SerialGraph.SerialPeriod;

import com.google.common.collect.ComparisonChain;

public final class ClassPeriod extends ProgramObject<SerialPeriod> implements
    Comparable<ClassPeriod> {
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
    return serial.getShortDescription();
  }

  /**
   * Returns a list containing {@code count} periods starting with this one.
   * 
   * @throws IndexOutOfBoundsException
   *           if there are not {@code count} periods in this time block starting with this one
   */
  public List<ClassPeriod> getTailPeriods(int count) {
    return getTimeBlock().getPeriods().subList(index, index + count);
  }

  @Override
  public int compareTo(ClassPeriod o) {
    return ComparisonChain
        .start()
        .compare(block.getId(), o.getTimeBlock().getId())
        .compare(index, o.getIndex())
        .result();
  }
}
