/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.learningu.scheduling.util.bst;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.learningu.scheduling.util.bst.BstSide.LEFT;
import static org.learningu.scheduling.util.bst.BstSide.RIGHT;

import com.google.common.collect.AbstractLinkedIterator;
import com.google.common.collect.BoundType;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

import javax.annotation.Nullable;

/**
 * Drop-in replacement for JDK 6 TreeMap that is JDK 5 compatible.
 * 
 * @author Louis Wasserman
 */
public final class BstMap<K, V> extends AbstractMap<K, V> implements SortedMap<K, V> {
  private static final Random RAND = new Random();

  final TreapNode<K, V> root;

  final GeneralRange<K> range;

  @SuppressWarnings("rawtypes")
  public static <K extends Comparable, V> BstMap<K, V> create() {
    return create(Ordering.natural());
  }

  public static <K, V> BstMap<K, V> create(Comparator<? super K> comparator) {
    return new BstMap<K, V>(null, GeneralRange.<K> all(comparator));
  }

  private BstMap(TreapNode<K, V> root, GeneralRange<K> range) {
    this.root = root;
    this.range = range;
  }

  private transient Set<Entry<K, V>> entrySet;

  @Override
  public Set<Entry<K, V>> entrySet() {
    Set<Entry<K, V>> result = entrySet;
    return (result == null) ? entrySet = createEntrySet() : result;
  }

  private Set<Entry<K, V>> createEntrySet() {
    return new AbstractSet<Entry<K, V>>() {
      @Override
      public Iterator<Entry<K, V>> iterator() {

        final BstInOrderPath<TreapNode<K, V>> startingPath = BstRangeOps.furthestPath(
            range,
            LEFT,
            pathFactory(),
            root);
        final Iterator<BstInOrderPath<TreapNode<K, V>>> pathIterator = new AbstractLinkedIterator<BstInOrderPath<TreapNode<K, V>>>(
            startingPath) {
          @Override
          protected BstInOrderPath<TreapNode<K, V>> computeNext(
              BstInOrderPath<TreapNode<K, V>> previous) {
            if (!previous.hasNext(RIGHT)) {
              return null;
            }
            BstInOrderPath<TreapNode<K, V>> next = previous.next(RIGHT);
            return range.contains(next.getTip().getKey()) ? next : null;
          }
        };
        return new Iterator<Entry<K, V>>() {
          K toRemove = null;

          @Override
          public boolean hasNext() {
            return pathIterator.hasNext();
          }

          @Override
          public Entry<K, V> next() {
            BstInOrderPath<TreapNode<K, V>> path = pathIterator.next();
            TreapNode<K, V> tip = path.getTip();
            toRemove = tip.getKey();
            return Maps.immutableEntry(tip.getKey(), tip.getValue());
          }

          @Override
          public void remove() {
            checkState(toRemove != null);
            BstMap.this.remove(toRemove);
            toRemove = null;
          }
        };
      }

      @Override
      public int size() {
        return BstMap.this.size();
      }
    };
  }

  @Override
  public V remove(@Nullable Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return Ints.saturatedCast(BstRangeOps.totalInRange(countAggregate, range, root));
  }

  @Override
  public boolean containsKey(@Nullable Object key) {
    return get(key) != null;
  }

  @Override
  public V get(@Nullable Object key) {
    if (key == null) {
      return null;
    }
    try {
      @SuppressWarnings("unchecked")
      K k = (K) key;
      if (range.contains(k)) {
        TreapNode<K, V> node = BstOperations.seek(range.comparator(), root, k);
        return (node == null) ? null : node.getValue();
      }
      return null;
    } catch (ClassCastException e) {
      return null;
    }
  }

  public BstMap<K, V> insert(K key, V value) {
    K k = (K) checkNotNull(key);
    checkArgument(range.contains(k));
    PutModifier<K, V> modifier = new PutModifier<K, V>(checkNotNull(value));
    return modify(k, modifier);
  }

  public BstMap<K, V> delete(@Nullable Object key) {
    if (key == null) {
      return this;
    }
    try {
      @SuppressWarnings("unchecked")
      K k = (K) key;
      if (range.contains(k)) {
        RemoveModifier<K, V> modifier = new RemoveModifier<K, V>();
        return modify(k, modifier);
      }
      return this;
    } catch (ClassCastException e) {
      return this;
    }
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  private BstMap<K, V> modify(K key, BstModifier<K, TreapNode<K, V>> modifier) {
    BstMutationRule<K, TreapNode<K, V>> mutationRule = BstMutationRule.createRule(
        modifier,
        TreapNode.<K, V> balancePolicy(),
        nodeFactory);
    BstMutationResult<K, TreapNode<K, V>> mutationResult = BstOperations.mutate(
        range.comparator(),
        mutationRule,
        root,
        key);
    return new BstMap<K, V>(mutationResult.getChangedRoot(), range);
  }

  public Entry<K, V> firstEntry() {
    BstInOrderPath<TreapNode<K, V>> firstPath = BstRangeOps.furthestPath(
        range,
        LEFT,
        pathFactory(),
        root);
    return tipOrNull(firstPath);
  }

  public Entry<K, V> lastEntry() {
    BstInOrderPath<TreapNode<K, V>> lastPath = BstRangeOps.furthestPath(
        range,
        RIGHT,
        pathFactory(),
        root);
    return tipOrNull(lastPath);
  }

  private static <K, V> Entry<K, V> tipOrNull(BstInOrderPath<TreapNode<K, V>> path) {
    return (path == null) ? null : Maps.immutableEntry(path.getTip().getKey(), path
        .getTip()
        .getValue());
  }

  @Override
  public Comparator<? super K> comparator() {
    return range.comparator();
  }

  @Override
  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    return subMap(fromKey, true, toKey, false);
  }

  @Override
  public SortedMap<K, V> headMap(K toKey) {
    return headMap(toKey, false);
  }

  @Override
  public SortedMap<K, V> tailMap(K fromKey) {
    return tailMap(fromKey, true);
  }

  @Override
  public K firstKey() {
    return keyOrThrow(firstEntry());
  }

  @Override
  public K lastKey() {
    return keyOrThrow(lastEntry());
  }

  private static <K> K keyOrThrow(@Nullable Entry<K, ?> entry) {
    if (entry == null) {
      throw new NoSuchElementException();
    }
    return entry.getKey();
  }

  public BstMap<K, V> headMap(K toKey, boolean inclusive) {
    return new BstMap<K, V>(root, range.intersect(GeneralRange.upTo(
        comparator(),
        checkNotNull(toKey),
        boundType(inclusive))));
  }

  public BstMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
    return new BstMap<K, V>(root, range.intersect(GeneralRange.range(
        comparator(),
        checkNotNull(fromKey),
        boundType(fromInclusive),
        checkNotNull(toKey),
        boundType(toInclusive))));
  }

  public BstMap<K, V> tailMap(K toKey, boolean inclusive) {
    return new BstMap<K, V>(root, range.intersect(GeneralRange.downTo(
        comparator(),
        checkNotNull(toKey),
        boundType(inclusive))));
  }

  public Entry<K, V> ceilingEntry(K key) {
    return tailMap(key, true).firstEntry();
  }

  public Entry<K, V> floorEntry(K key) {
    return headMap(key, true).lastEntry();
  }

  public Entry<K, V> higherEntry(K key) {
    return tailMap(key, false).firstEntry();
  }

  public Entry<K, V> lowerEntry(K key) {
    return headMap(key, false).lastEntry();
  }

  private static BoundType boundType(boolean inclusive) {
    return inclusive ? BoundType.CLOSED : BoundType.OPEN;
  }

  private static final class PutModifier<K, V> implements BstModifier<K, TreapNode<K, V>> {
    private final V newValue;

    private PutModifier(V value) {
      this.newValue = value;
    }

    @Override
    public BstModificationResult<TreapNode<K, V>> modify(
        K key,
        @Nullable TreapNode<K, V> originalEntry) {
      if (originalEntry == null) {
        return BstModificationResult.rebalancingChange(null, new TreapNode<K, V>(key, newValue,
            null, null, RAND.nextInt()));
      } else {
        return BstModificationResult.rebuildingChange(originalEntry, new TreapNode<K, V>(key,
            newValue, null, null, originalEntry.heapKey));
      }
    }
  }

  private static final class RemoveModifier<K, V> implements BstModifier<K, TreapNode<K, V>> {
    private RemoveModifier() {
    }

    @Override
    public BstModificationResult<TreapNode<K, V>> modify(
        K key,
        @Nullable TreapNode<K, V> originalEntry) {
      if (originalEntry == null) {
        return BstModificationResult.identity(null);
      } else {
        originalEntry.getValue();
        return BstModificationResult.rebalancingChange(originalEntry, null);
      }
    }
  }

  private BstPathFactory<TreapNode<K, V>, BstInOrderPath<TreapNode<K, V>>> pathFactory() {
    return BstInOrderPath.inOrderFactory();
  }

  private transient final BstNodeFactory<TreapNode<K, V>> nodeFactory = TreapNode.nodeFactory();

  private transient final BstAggregate<TreapNode<K, V>> countAggregate = new BstAggregate<TreapNode<K, V>>() {
    @Override
    public long treeValue(@Nullable TreapNode<K, V> tree) {
      return (tree == null) ? 0 : tree.size;
    }

    @Override
    public int entryValue(@Nullable TreapNode<K, V> entry) {
      return (entry == null) ? 0 : 1;
    }
  };
}
