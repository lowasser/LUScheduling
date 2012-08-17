package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import java.util.List;

import org.learningu.scheduling.graph.SerialGraph.SerialPeriod;
import org.learningu.scheduling.graph.SerialGraph.SerialTimeBlock;

/**
 * A block of time at an LU program, in which a single section of a single course might be
 * scheduled.
 * 
 * @author lowasser
 */
public final class TimeBlock extends ProgramObject<SerialTimeBlock> implements
    Comparable<TimeBlock> {

  private final ImmutableList<ClassPeriod> periods;

  TimeBlock(Program program, SerialTimeBlock serial) {
    super(program, serial);
    ImmutableList.Builder<ClassPeriod> builder = ImmutableList.builder();
    int index = 0;
    for (SerialPeriod period : serial.getPeriodList()) {
      builder.add(new ClassPeriod(period, this, index++));
    }
    periods = builder.build();
  }

  @Override
  public int getId() {
    return serial.getBlockId();
  }

  public String getDescription() {
    return serial.getDescription();
  }

  public List<ClassPeriod> getPeriods() {
    return periods;
  }

  public ClassPeriod getPeriod(int index) {
    return periods.get(index);
  }

  static Function<SerialTimeBlock, TimeBlock> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<SerialTimeBlock, TimeBlock>() {
      @Override
      public TimeBlock apply(SerialTimeBlock input) {
        return new TimeBlock(program, input);
      }
    };
  }

  @Override
  public int compareTo(TimeBlock o) {
    checkArgument(getProgram() == o.getProgram());
    return getId() - o.getId();
  }

  @Override
  public String getShortDescription() {
    return getDescription();
  }

}
