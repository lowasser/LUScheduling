package org.learningu.scheduling.util;

import com.google.common.collect.ForwardingSortedMap;
import com.google.common.collect.ForwardingSortedSet;
import com.google.common.collect.Iterators;

import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.SortedSet;

public final class Navigables {
  private Navigables() {
  }

  public static <E> NavigableSet<E> unmodifiableNavigableSet(NavigableSet<E> set) {
    if (set instanceof UnmodifiableNavigableSet) {
      return set;
    } else {
      return new UnmodifiableNavigableSet<E>(set);
    }
  }

  private static final class UnmodifiableNavigableSet<E> extends ForwardingSortedSet<E> implements
      NavigableSet<E> {
    private final NavigableSet<E> delegate;

    private UnmodifiableNavigableSet(NavigableSet<E> delegate) {
      this.delegate = delegate;
    }

    @Override
    protected SortedSet<E> delegate() {
      return Collections.unmodifiableSortedSet(delegate);
    }

    @Override
    public E lower(E e) {
      return delegate.lower(e);
    }

    @Override
    public E floor(E e) {
      return delegate.floor(e);
    }

    @Override
    public E ceiling(E e) {
      return delegate.ceiling(e);
    }

    @Override
    public E higher(E e) {
      return delegate.higher(e);
    }

    @Override
    public E pollFirst() {
      throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<E> descendingSet() {
      return unmodifiableNavigableSet(delegate.descendingSet());
    }

    @Override
    public Iterator<E> descendingIterator() {
      return Iterators.unmodifiableIterator(delegate.descendingIterator());
    }

    @Override
    public NavigableSet<E> subSet(
        E fromElement,
        boolean fromInclusive,
        E toElement,
        boolean toInclusive) {
      return unmodifiableNavigableSet(delegate.subSet(
          fromElement,
          fromInclusive,
          toElement,
          toInclusive));
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
      return unmodifiableNavigableSet(delegate.headSet(toElement, inclusive));
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
      return unmodifiableNavigableSet(delegate.tailSet(fromElement, inclusive));
    }
  }

  public static <K, V> NavigableMap<K, V> unmodifiableNavigableMap(NavigableMap<K, V> map) {
    if (map instanceof UnmodifiableNavigableMap) {
      return map;
    } else {
      return new UnmodifiableNavigableMap<K, V>(map);
    }
  }

  private static final class UnmodifiableNavigableMap<K, V> extends ForwardingSortedMap<K, V>
      implements NavigableMap<K, V> {
    private final NavigableMap<K, V> delegate;

    private UnmodifiableNavigableMap(NavigableMap<K, V> delegate) {
      this.delegate = delegate;
    }

    @Override
    protected SortedMap<K, V> delegate() {
      return Collections.unmodifiableSortedMap(delegate);
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
      return delegate.lowerEntry(key);
    }

    @Override
    public K lowerKey(K key) {
      return delegate.lowerKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
      return delegate.floorEntry(key);
    }

    @Override
    public K floorKey(K key) {
      return delegate.floorKey(key);
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
      return delegate.ceilingEntry(key);
    }

    @Override
    public K ceilingKey(K key) {
      return delegate.ceilingKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
      return delegate.higherEntry(key);
    }

    @Override
    public K higherKey(K key) {
      return delegate.higherKey(key);
    }

    @Override
    public Entry<K, V> firstEntry() {
      return delegate.firstEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
      return delegate.lastEntry();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
      throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
      return unmodifiableNavigableMap(delegate.descendingMap());
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
      return unmodifiableNavigableSet(delegate.navigableKeySet());
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
      return unmodifiableNavigableSet(delegate.descendingKeySet());
    }

    @Override
    public
        NavigableMap<K, V>
        subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      return unmodifiableNavigableMap(delegate.subMap(fromKey, fromInclusive, toKey, toInclusive));
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
      return unmodifiableNavigableMap(delegate.headMap(toKey, inclusive));
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
      return unmodifiableNavigableMap(delegate.tailMap(fromKey, inclusive));
    }
  }
}
