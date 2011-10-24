package org.learningu.scheduling.util;

import java.util.Iterator;
import java.util.NavigableSet;

import com.google.common.collect.ForwardingSortedSet;

public abstract class ForwardingNavigableSet<E> extends ForwardingSortedSet<E> implements
    NavigableSet<E> {

  @Override
  public E lower(E e) {
    return delegate().lower(e);
  }

  @Override
  public E floor(E e) {
    return delegate().floor(e);
  }

  @Override
  public E ceiling(E e) {
    return delegate().ceiling(e);
  }

  @Override
  public E higher(E e) {
    return delegate().higher(e);
  }

  @Override
  public E pollFirst() {
    return delegate().pollFirst();
  }

  @Override
  public E pollLast() {
    return delegate().pollLast();
  }

  @Override
  public NavigableSet<E> descendingSet() {
    return delegate().descendingSet();
  }

  @Override
  public Iterator<E> descendingIterator() {
    return delegate().descendingIterator();
  }

  @Override
  public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement,
      boolean toInclusive) {
    return delegate().subSet(fromElement, fromInclusive, toElement, toInclusive);
  }

  @Override
  public NavigableSet<E> headSet(E toElement, boolean inclusive) {
    return delegate().headSet(toElement, inclusive);
  }

  @Override
  public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
    return delegate().tailSet(fromElement, inclusive);
  }

  @Override
  protected abstract NavigableSet<E> delegate();

}
