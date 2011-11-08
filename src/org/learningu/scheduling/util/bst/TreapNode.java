package org.learningu.scheduling.util.bst;

import static org.learningu.scheduling.util.bst.BstSide.LEFT;
import static org.learningu.scheduling.util.bst.BstSide.RIGHT;

import javax.annotation.Nullable;

public final class TreapNode<K, V> extends BstNode<K, TreapNode<K, V>> {
  private final V value;

  final int heapKey;

  final int size;

  TreapNode(
      K key,
      V value,
      @Nullable TreapNode<K, V> left,
      @Nullable TreapNode<K, V> right,
      int heapKey) {
    super(key, left, right);
    this.value = value;
    this.heapKey = heapKey;
    this.size = 1 + ((left == null) ? 0 : left.size) + ((right == null) ? 0 : right.size);
  }

  public V getValue() {
    return value;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <K, V> BstNodeFactory<TreapNode<K, V>> nodeFactory() {
    return (BstNodeFactory) NODE_FACTORY;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <K, V> BstBalancePolicy<TreapNode<K, V>> balancePolicy() {
    return (BstBalancePolicy) BALANCE_POLICY;
  }

  private static final BstNodeFactory<TreapNode<Object, Object>> NODE_FACTORY = new BstNodeFactory<TreapNode<Object, Object>>() {

    @Override
    public TreapNode<Object, Object> createNode(
        TreapNode<Object, Object> source,
        TreapNode<Object, Object> left,
        TreapNode<Object, Object> right) {
      return new TreapNode<Object, Object>(source.getKey(), source.getValue(), left, right,
          source.heapKey);
    }

  };

  private static final BstBalancePolicy<TreapNode<Object, Object>> BALANCE_POLICY = new BstBalancePolicy<TreapNode<Object, Object>>() {

    @Override
    public TreapNode<Object, Object> balance(
        BstNodeFactory<TreapNode<Object, Object>> nodeFactory,
        TreapNode<Object, Object> source,
        @Nullable TreapNode<Object, Object> left,
        @Nullable TreapNode<Object, Object> right) {
      if (left != null && left.heapKey < source.heapKey
          && (right == null || right.heapKey >= left.heapKey)) {
        return nodeFactory.createNode(
            left,
            left.childOrNull(LEFT),
            balance(nodeFactory, source, left.childOrNull(RIGHT), right));
      } else if (right != null && right.heapKey < source.heapKey) {
        return nodeFactory.createNode(
            right,
            balance(nodeFactory, source, left, right.childOrNull(LEFT)),
            right.childOrNull(RIGHT));
      } else {
        return nodeFactory.createNode(source, left, right);
      }
    }

    @Override
    public TreapNode<Object, Object> combine(
        BstNodeFactory<TreapNode<Object, Object>> nodeFactory,
        @Nullable TreapNode<Object, Object> left,
        @Nullable TreapNode<Object, Object> right) {
      if (left == null) {
        return right;
      } else if (right == null) {
        return left;
      } else if (left.heapKey <= right.heapKey) {
        return nodeFactory.createNode(
            left,
            left.childOrNull(LEFT),
            combine(nodeFactory, left.childOrNull(RIGHT), right));
      } else {
        return nodeFactory.createNode(
            right,
            combine(nodeFactory, left, right.childOrNull(LEFT)),
            right.childOrNull(RIGHT));
      }
    }

  };

}
