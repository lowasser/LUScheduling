package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;

import org.learningu.scheduling.graph.SerialGraph.SerialResource;

/**
 * A property of a room that some courses may require. If the property is <i>binding</i>, no course
 * that doesn't require this property may use a room with this property.
 * 
 * @author lowasser
 */
public final class Resource extends ProgramObject<SerialResource> {
  public static final Predicate<Resource> IS_BINDING = new Predicate<Resource>() {
    @Override
    public boolean apply(Resource input) {
      return input.isBinding();
    }
  };

  private Resource(Program program, SerialResource serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getResourceId();
  }

  public String getDescription() {
    return serial.getDescription();
  }

  public boolean isBinding() {
    return serial.getIsBinding();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("description", serial.getDescription()).toString();
  }

  static Function<SerialResource, Resource> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<SerialResource, Resource>() {
      @Override
      public Resource apply(SerialResource input) {
        return new Resource(program, input);
      }
    };
  }

}
