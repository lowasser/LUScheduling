package org.learningu.scheduling.util.bst;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedMap.Builder;
import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.SortedMapInterfaceTest;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

public class BstMapTest extends SortedMapInterfaceTest<String, String> {
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(MapTestSuiteBuilder
        .using(new TestStringMapGenerator() {

          @Override
          protected Map<String, String> create(Entry<String, String>[] entries) {
            BstMap<String, String> map = BstMap.create();
            for (Entry<String, String> entry : entries) {
              map = map.insert(entry.getKey(), entry.getValue());
            }
            return map;
          }

          @Override
          public Iterable<Entry<String, String>> order(List<Entry<String, String>> insertionOrder) {
            Builder<String, String> builder = ImmutableSortedMap.naturalOrder();
            for (Entry<String, String> entry : insertionOrder) {
              builder.put(entry.getKey(), entry.getValue());
            }
            return builder.build().entrySet();
          }
        })
        .named("BstMap")
        .withFeatures(CollectionSize.ANY, CollectionFeature.KNOWN_ORDER)
        .createTestSuite());
    suite.addTestSuite(BstMapTest.class);
    return suite;
  }

  public BstMapTest() {
    super(false, false, false, false, false);
  }

  @Override
  protected SortedMap<String, String> makeEmptyMap() throws UnsupportedOperationException {
    return BstMap.create();
  }

  @Override
  protected SortedMap<String, String> makePopulatedMap() throws UnsupportedOperationException {
    BstMap<String, String> map = BstMap.create();
    map = map.insert("a", "1");
    map = map.insert("b", "2");
    map = map.insert("d", "4");
    map = map.insert("e", "5");
    return map;
  }

  @Override
  protected String getKeyNotInPopulatedMap() throws UnsupportedOperationException {
    return "c";
  }

  @Override
  protected String getValueNotInPopulatedMap() throws UnsupportedOperationException {
    return "3";
  }
}
