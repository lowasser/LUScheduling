package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;

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
abstract class ProgramObject<T extends Message & MessageOrBuilder> {
  final Program program;
  final T serial;

  ProgramObject(Program program, T serial) {
    this.program = checkNotNull(program);
    this.serial = checkNotNull(serial);
  }

  public abstract int getId();

  public String toString() {
    return TextFormat.printToString(serial);
  }

  @Override
  public int hashCode() {
    return getId();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof ProgramObject) {
      ProgramObject<?> other = (ProgramObject<?>) obj;
      return program == other.program && getId() == other.getId() && getClass().equals(other.getClass());
    }
    return false;
  }
}
