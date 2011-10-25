package org.learningu.scheduling.logic;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.Flag;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.StartAssignment;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A cumulative validator for schedules, accumulating any conflicts found for later analysis or
 * corrective action.
 * 
 * @author lowasser
 */
public final class ScheduleValidator {
  private final List<LocalConflict<StartAssignment>> localStartConflicts;

  private final List<LocalConflict<PresentAssignment>> localPresentConflicts;

  private final List<GlobalConflict<PresentAssignment>> globalPresentConflicts;

  private final List<GlobalConflict<StartAssignment>> globalStartConflicts;

  private final Logger logger;

  @Flag(
      value = "validateLogLevel",
      defaultValue = "FINEST",
      description = "Level at which to log each validation attempt")
  private final Level validateLogLevel;

  @Flag(
      value = "validateFailureLogLevel",
      defaultValue = "FINER",
      description = "Level at which to log failed validations")
  private final Level failureLogLevel;

  @Inject
  ScheduleValidator(Logger logger, @Named("validateLogLevel") Level validateLogLevel,
      @Named("validateFailureLogLevel") Level failureLogLevel) {
    this.logger = logger;
    this.validateLogLevel = validateLogLevel;
    this.failureLogLevel = failureLogLevel;
    this.localStartConflicts = Lists.newArrayList();
    this.localPresentConflicts = Lists.newArrayList();
    this.globalPresentConflicts = Lists.newArrayList();
    this.globalStartConflicts = Lists.newArrayList();
  }

  public List<LocalConflict<StartAssignment>> getLocalStartConflicts() {
    return localStartConflicts;
  }

  public List<LocalConflict<PresentAssignment>> getLocalPresentConflicts() {
    return localPresentConflicts;
  }

  public List<GlobalConflict<PresentAssignment>> getGlobalPresentConflicts() {
    return globalPresentConflicts;
  }

  public List<GlobalConflict<StartAssignment>> getGlobalStartConflicts() {
    return globalStartConflicts;
  }

  void log(Level level, String message, Object... params) {
    logger.log(level, message, params);
  }

  public void validateLocal(boolean cond, StartAssignment assignment, String condition) {
    log(
        validateLogLevel,
        "Doing local validation on {0} for condition: {1}",
        assignment,
        condition);
    if (!cond) {
      LocalConflict<StartAssignment> conflict = LocalConflict.create(assignment, condition);
      log(failureLogLevel, "Validation failed: {0}", conflict);
      localStartConflicts.add(conflict);
    }
  }

  public void validateLocal(boolean cond, PresentAssignment assignment, String condition) {
    log(
        validateLogLevel,
        "Doing local validation on {0} for condition: {1}",
        assignment,
        condition);
    if (!cond) {
      LocalConflict<PresentAssignment> conflict = LocalConflict.create(assignment, condition);
      log(failureLogLevel, "Validation failed: {0}", conflict);
      localPresentConflicts.add(conflict);
    }
  }

  public void validateGlobal(StartAssignment assignment, Iterable<StartAssignment> conflicting,
      String condition) {
    validateGlobal(false, assignment, conflicting, condition);
  }

  public void validateGlobal(boolean cond, StartAssignment assignment,
      Iterable<StartAssignment> conflicting, String condition) {
    log(
        validateLogLevel,
        "Doing global validation on {0} for condition: {1}",
        assignment,
        condition);
    if (!cond && !Iterables.isEmpty(conflicting)) {
      GlobalConflict<StartAssignment> conflict = GlobalConflict.create(
          assignment,
          conflicting,
          condition);
      log(failureLogLevel, "Validation failed: {0}", conflict);
      globalStartConflicts.add(conflict);
    }
  }

  public void validateGlobal(PresentAssignment assignment,
      Iterable<PresentAssignment> conflicting, String condition) {
    validateGlobal(false, assignment, conflicting, condition);
  }

  public void validateGlobal(boolean cond, PresentAssignment assignment,
      Iterable<PresentAssignment> conflicting, String condition) {
    log(
        validateLogLevel,
        "Doing global validation on {0} for condition: {1}",
        assignment,
        condition);
    if (!cond && !Iterables.isEmpty(conflicting)) {
      GlobalConflict<PresentAssignment> conflict = GlobalConflict.create(
          assignment,
          conflicting,
          condition);
      log(failureLogLevel, "Validation failed: {0}", conflict);
      globalPresentConflicts.add(conflict);
    }
  }

  public boolean isLocallyValid() {
    return localStartConflicts.isEmpty() && localPresentConflicts.isEmpty();
  }

  public boolean isValid() {
    return isLocallyValid() && globalPresentConflicts.isEmpty() && globalStartConflicts.isEmpty();
  }

  @Override
  public String toString() {
    ToStringHelper helper = Objects.toStringHelper(this);
    if (!localStartConflicts.isEmpty()) {
      helper.add("localStartConflicts", localStartConflicts);
    }
    if (!localPresentConflicts.isEmpty()) {
      helper.add("localPresentConflicts", localPresentConflicts);
    }
    if (!globalPresentConflicts.isEmpty()) {
      helper.add("globalPresentConflicts", globalPresentConflicts);
    }
    if (!globalStartConflicts.isEmpty()) {
      helper.add("globalStartConflicts", globalStartConflicts);
    }
    return helper.toString();
  }
}
