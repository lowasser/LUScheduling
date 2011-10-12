package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;

abstract class ProgramObject<T extends Message & MessageOrBuilder> implements HasUID {
  final Program program;
  final T serial;

  ProgramObject(Program program, T serial) {
    this.program = checkNotNull(program);
    this.serial = checkNotNull(serial);
  }

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
      return getId() == other.getId() && serial.getAllFields().equals(other.serial.getAllFields());
    }
    return false;
  }
}
