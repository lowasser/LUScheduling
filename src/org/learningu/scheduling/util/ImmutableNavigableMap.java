package org.learningu.scheduling.util;

import java.util.NavigableMap;

import com.google.common.collect.Maps;

public final class ImmutableNavigableMap<K, V> extends ForwardingNavigableMap<K, V> {
  public static <K, V> ImmutableNavigableMap<K, V> copyOf(NavigableMap<K, V> map) {
    if (map instanceof ImmutableNavigableMap) {
      return (ImmutableNavigableMap<K, V>) map;
    } else {
      return new ImmutableNavigableMap<K, V>(Navigables.unmodifiableNavigableMap(Maps
          .newTreeMap(map)));
    }
  }

  private final NavigableMap<K, V> delegate;

  private ImmutableNavigableMap(NavigableMap<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  protected NavigableMap<K, V> delegate() {
    return delegate;
  }
}
