package org.learningu.scheduling.logic;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.PresentAssignment;
import org.learningu.scheduling.StartAssignment;
import org.learningu.scheduling.util.Flag;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public final class ScheduleValidator {
  private final List<LocalConflict<PresentAssignment>> localConflicts;
  private final List<GlobalConflict<PresentAssignment>> globalPresentConflicts;
  private final List<GlobalConflict<StartAssignment>> globalStartConflicts;

  private final Logger logger;

  @Flag(value = "validateLogLevel", defaultValue = "FINEST", description = "Level at which to log each validation attempt")
  private final Level validateLogLevel;

  @Flag(value = "validateFailureLogLevel", defaultValue = "FINER", description = "Level at which to log failed validations")
  private final Level failureLogLevel;

  @Inject
  ScheduleValidator(Logger logger, @Named("validateLogLevel") Level validateLogLevel,
      @Named("validateFailureLogLevel") Level failureLogLevel) {
    this.logger = logger;
    this.validateLogLevel = validateLogLevel;
    this.failureLogLevel = failureLogLevel;
    this.localConflicts = Lists.newArrayList();
    this.globalPresentConflicts = Lists.newArrayList();
    this.globalStartConflicts = Lists.newArrayList();
  }

  private void log(Level level, String message, Object... params) {
    logger.log(level, message, params);
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
      localConflicts.add(conflict);
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

  public boolean isValid() {
    return localConflicts.isEmpty() && globalPresentConflicts.isEmpty()
        && globalStartConflicts.isEmpty();
  }

  @Override
  public String toString() {
    ToStringHelper helper = Objects.toStringHelper(this);
    if (!localConflicts.isEmpty()) {
      helper.add("localConflicts", localConflicts);
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
