package org.learningu.scheduling.optimization;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Predicate;

public final class RetryingPerturber<T> implements Perturber<T> {
  public static <T> RetryingPerturber<T> create(Predicate<T> valid, Perturber<T> delegate) {
    return new RetryingPerturber<T>(valid, delegate);
  }

  private final Predicate<T> valid;
  private final Perturber<T> delegate;

  private RetryingPerturber(Predicate<T> valid, Perturber<T> delegate) {
    this.valid = checkNotNull(valid);
    this.delegate = checkNotNull(delegate);
  }

  @Override
  public T perturb(T initial, double temperature) {
    T result;
    do {
      result = delegate.perturb(initial, temperature);
    } while (!valid.apply(result));
    return result;
  }

}
