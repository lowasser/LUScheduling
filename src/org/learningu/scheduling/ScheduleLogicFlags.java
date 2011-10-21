package org.learningu.scheduling;

import org.learningu.scheduling.util.Flag;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * An injectable collection of flags for scheduling logic.
 * 
 * @author lowasser
 */
public final class ScheduleLogicFlags {
  @Flag(value = "minEstimatedClassSizeRatio", description = "The smallest estimated class size : "
      + "room capacity ratio that is acceptable.  For example, if this was 0.5, a class estimated "
      + "to have 15 students would not be allowed into a room with capacity over 30."
      + " Default is 0.", defaultValue = "0.0")
  final double minEstimatedClassSizeRatio;
  @Flag(value = "maxEstimatedClassSizeRatio", description = "The greatest estimated class size : "
      + "room capacity ratio that is acceptable.  For example, if this was 0.5, a class estimated "
      + "to have 15 students would not be allowed into a room with capacity under 30."
      + " Default is 1.", defaultValue = "1.0")
  final double maxEstimatedClassSizeRatio;
  @Flag(value = "maxClassCapRatio", description = "The greatest class class cap : "
      + "room capacity ratio that is acceptable.  For example, if this was 0.5, a class with a "
      + " cap of 15 students would not be allowed into a room with capacity under 30."
      + " Default is 1.", defaultValue = "1.0")
  final double maxClassCapRatio;

  @Inject
  ScheduleLogicFlags(
      @Named("minEstimatedClassSizeRatio") double minEstimatedClassSizeRatio,
      @Named("maxEstimatedClassSizeRatio") double maxEstimatedClassSizeRatio,
      @Named("maxClassCapRatio") double maxClassCapRatio) {
    this.minEstimatedClassSizeRatio = minEstimatedClassSizeRatio;
    this.maxEstimatedClassSizeRatio = maxEstimatedClassSizeRatio;
    this.maxClassCapRatio = maxClassCapRatio;
  }
}