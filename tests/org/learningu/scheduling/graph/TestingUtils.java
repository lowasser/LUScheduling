package org.learningu.scheduling.graph;

import junit.framework.Assert;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

public class TestingUtils {

  public static void assertMessageEquals(Message expected, Message actual) {
    Assert.assertEquals(expected.getAllFields().keySet(), actual.getAllFields().keySet());

    for (FieldDescriptor descriptor : expected.getAllFields().keySet()) {
      Object expectedObj = expected.getField(descriptor);
      Object actualObj = actual.getField(descriptor);
      Assert.assertEquals(expectedObj, actualObj);
    }
  }
}
