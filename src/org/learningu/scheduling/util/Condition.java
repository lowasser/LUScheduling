package org.learningu.scheduling.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.annotation.Nullable;

import junit.framework.AssertionFailedError;

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
  private final StringBuilder builder = new StringBuilder();

  @Nullable
  private final Condition parent;

  private Condition(Logger logger, Level level, @Nullable Condition parent) {
    this.level = checkNotNull(level);
    this.logger = checkNotNull(logger);
    this.isValid = true;
    this.parent = parent;
    logger.addHandler(new Handler() {
      @Override
      public void close() throws SecurityException {
      }

      @Override
      public void flush() {
      }

      @Override
      public void publish(LogRecord record) {
        builder.append(
            String.format(new SimpleFormatter().formatMessage(record), record.getParameters()))
            .append('\n');
      }
    });
  }

  public Logger getLogger() {
    return logger;
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

  public boolean passes() {
    return isValid;
  }

  public void assertPasses() {
    if (!isValid) {
      throw new AssertionFailedError(builder.toString());
    }
  }
}
