package org.learningu.scheduling.util;

public final class ModifiedState<A, S> {
  public static <A, S> ModifiedState<A, S> of(A result, S newState) {
    return new ModifiedState<A, S>(result, newState);
  }

  private final A result;

  private final S newState;

  private ModifiedState(A result, S newState) {
    this.result = result;
    this.newState = newState;
  }

  public A getResult() {
    return result;
  }

  public S getNewState() {
    return newState;
  }
}
