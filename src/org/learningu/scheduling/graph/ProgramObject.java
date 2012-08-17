package org.learningu.scheduling.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;

import javax.annotation.Nullable;

/**
 * A component in the object graph associated with an LU program, with its own protocol buffer.
 * Attached to a {@code Program} so that it can get the objects associated with ID numbers.
 * 
 * <p>
 * For example, the protobuf associated with a course does not directly point to the teachers of
 * the course; instead, it just lists their ID numbers. The course program object lazily looks up
 * teachers from their ID numbers from the {@code Program} object graph.
 * 
 * <p>
 * This also provides basic hashing, equality, and {@code toString} functionality based on the
 * protocol buffer.
 * 
 * @author lowasser
 */
public abstract class ProgramObject<T extends Message & MessageOrBuilder> {
  final Program program;
  final T serial;

  ProgramObject(Program program, T serial) {
    this.program = checkNotNull(program);
    this.serial = checkNotNull(serial);
  }
  
  public abstract String getShortDescription();

  public abstract int getId();

  @Override
  public String toString() {
    return TextFormat.printToString(serial);
  }

  @Override
  public int hashCode() {
    return getId() ^ getClass().hashCode();
  }

  public Program getProgram() {
    return program;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof ProgramObject) {
      ProgramObject<?> other = (ProgramObject<?>) obj;
      return program == other.program && getId() == other.getId()
          && getClass().equals(other.getClass());
    }
    return false;
  }
}
