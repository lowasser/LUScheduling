package org.learningu.scheduling.logic;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.util.Flag;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public final class ScheduleValidator {
  private List<LocalConflict> localConflicts;
  private List<Conflict> conflicts;
  private final Logger logger;

  @Flag(
      value = "validatorLoggingLevel",
      description = "Logging level for ScheduleValidators.",
      defaultValue = "FINE")
  private final Level validatorLevel;

  private static final Level DETAIL_LEVEL = Level.FINEST;

  @Inject
  ScheduleValidator(Logger logger, @Named("validatorLoggingLevel") Level level) {
    this.localConflicts = Lists.newArrayList();
    this.conflicts = Lists.newArrayList();
    this.logger = Logger.getLogger("ScheduleValidator");
    this.validatorLevel = level;
  }

  public boolean isValid() {
    return localConflicts.isEmpty() && conflicts.isEmpty();
  }

  public void logDetail(String message, Object... arguments) {
    if (logger.isLoggable(DETAIL_LEVEL)) {
      logger.log(DETAIL_LEVEL, String.format(message, arguments));
    }
  }

  public void localValidate(
      boolean condition,
      Table.Cell<ClassPeriod, Room, Section> cell,
      String explanation) {
    logDetail("Testing %s for %s", cell, explanation);
    if (!condition) {
      LocalConflict conflict = LocalConflict.create(cell, explanation);
      localConflicts.add(conflict);
      logger.log(validatorLevel, "Schedule assignment failed: " + conflict);
    }
  }

  public void validate(
      Table.Cell<ClassPeriod, Room, Section> cell,
      Iterable<? extends Table.Cell<ClassPeriod, Room, Section>> conflicting,
      String explanation) {
    logDetail("Testing %s for %s", cell, explanation);
    ImmutableSet<Cell<ClassPeriod, Room, Section>> conflictSet = ImmutableSet.copyOf(conflicting);
    if (!conflictSet.isEmpty()) {
      Conflict conflict = Conflict.create(cell, conflicting, explanation);
      conflicts.add(conflict);
      logger.log(validatorLevel, "Schedule assignment failed: " + conflict);
    }
  }
}
