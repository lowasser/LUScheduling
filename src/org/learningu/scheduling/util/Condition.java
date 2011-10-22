package org.learningu.scheduling.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * A utility for testing for and logging failures.
 * 
 * @author lowasser
 */
public final class Condition {
  public static Condition create(Logger logger, Level level) {
    return new Condition(logger, level, null);
  }

  private boolean isValid;
  private final Logger logger;
  private final Level level;

  @Nullable
  private final Condition parent;

  private Condition(Logger logger, Level level, @Nullable Condition parent) {
    this.level = checkNotNull(level);
    this.logger = checkNotNull(logger);
    this.isValid = true;
    this.parent = parent;
  }

  public Condition createSubCondition(String subsystem) {
    Logger subLogger = Logger.getLogger(this.logger.getName() + "." + subsystem);
    return new Condition(subLogger, level, this);
  }

  public void verify(boolean condition, String message, Object... parameters) {
    if (!condition) {
      logger.log(level, message, parameters);
      for (Condition current = this; current != null; current = current.parent) {
        current.isValid = false;
      }
    }
  }

  public boolean isValid() {
    return isValid;
  }
}
