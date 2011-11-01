package org.learningu.scheduling.flags;

public interface Converter<T> {
  T parse(String string);
}
