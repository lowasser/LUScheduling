package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import com.google.common.base.Function;
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
    checkNotNull(collection);
    Builder<Integer, T> builder = ImmutableMap.builder();
    for (T t : collection) {
      checkNotNull(t);
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

  public Function<Integer, T> asLookupFunction() {
    return new Function<Integer, T>() {

      @Override
      public T apply(Integer input) {
        T t = idMap.get(input);
        checkArgument(t != null, "Has no value for id %s", input);
        return t;
      }
    };
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
