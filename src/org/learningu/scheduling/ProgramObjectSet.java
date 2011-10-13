package org.learningu.scheduling;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * A homogenous set of program objects, such as the set of all teachers.
 * 
 * @author lowasser
 */
final class ProgramObjectSet<T extends ProgramObject<?>> extends AbstractSet<T> {
  private final ImmutableMap<Integer, T> idMap;

  static <T extends ProgramObject<?>> ProgramObjectSet<T> create(Collection<T> collection) {
    Builder<Integer, T> builder = ImmutableMap.builder();
    for (T t : collection) {
      builder.put(t.getId(), t);
    }
    return new ProgramObjectSet<T>(builder.build());
  }

  private ProgramObjectSet(ImmutableMap<Integer, T> idMap) {
    this.idMap = idMap;
  }

  public T getForId(int id) {
    return idMap.get(id);
  }

  public boolean containsId(int id) {
    return idMap.containsKey(id);
  }

  @Override
  public boolean contains(@Nullable Object o) {
    if (o instanceof ProgramObject<?>) {
      ProgramObject<?> obj = (ProgramObject<?>) o;
      T lookup = idMap.get(obj.getId());
      return lookup != null && lookup.equals(o);
    }
    return false;
  }

  @Override
  public Iterator<T> iterator() {
    return idMap.values().iterator();
  }

  @Override
  public int size() {
    return idMap.size();
  }
}
