package org.learningu.scheduling;

import org.learningu.scheduling.annotations.ClassWithFlags;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class FlagsModule extends AbstractModule {
  public static FlagsModule create(Class<?> flagsClass) {
    return new FlagsModule(flagsClass);
  }

  private final Class<?> flagsClass;

  private FlagsModule(Class<?> flagsClass) {
    this.flagsClass = flagsClass;
  }

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Class.class, ClassWithFlags.class)
        .addBinding()
        .toInstance(flagsClass);
  }
}
