package org.learningu.scheduling;

import javax.annotation.Nullable;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;

abstract class ProgramObject<T extends Message & MessageOrBuilder> implements HasUID {
  final Program program;
  final T serial;

  ProgramObject(Program program, T serial) {
    this.program = program;
    this.serial = serial;
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
      return this.getClass() == obj.getClass() && this.getId() == ((HasUID) obj).getId();
    }
    return false;
  }
}
