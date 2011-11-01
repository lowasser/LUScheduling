package org.learningu.scheduling;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.FutureCallback;

public abstract class BasicFutureCallback<V> implements FutureCallback<V> {
  protected final Logger logger;

  BasicFutureCallback(Logger logger) {
    this.logger = logger;
  }

  public abstract void process(V value) throws Exception;

  @Override
  public void onSuccess(V result) {
    try {
      process(result);
    } catch (Exception e) {
      onFailure(e);
    }
  }

  @Override
  public void onFailure(Throwable t) {
    logger.log(Level.SEVERE, "Failure in future callback " + toString(), t);
  }
}
