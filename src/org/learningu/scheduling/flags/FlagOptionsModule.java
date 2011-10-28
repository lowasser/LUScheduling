package org.learningu.scheduling.flags;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.learningu.scheduling.annotations.ClassWithFlags;
import org.learningu.scheduling.annotations.Flag;
import org.learningu.scheduling.annotations.RuntimeArguments;

public final class FlagOptionsModule extends AbstractModule {

  static FlagOptionsModule create(String... args) {
    return new FlagOptionsModule(ImmutableList.copyOf(args));
  }

  private final ImmutableList<String> args;

  private FlagOptionsModule(ImmutableList<String> args) {
    this.args = args;
  }

  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  @RuntimeArguments
  List<String> args() {
    return args;
  }

  static final class FlagSpec {
    final Options options;

    final Map<Flag, Field> flagMap;

    FlagSpec(Options options, Map<Flag, Field> flagMap) {
      this.options = options;
      this.flagMap = flagMap;
    }
  }

  @Provides
  Options options(FlagSpec spec) {
    return spec.options;
  }

  @SuppressWarnings("rawtypes")
  @Provides
  @Singleton
  FlagSpec flagSpec(@ClassWithFlags Set<Class> flagClasses) {
    Options options = new Options();
    ImmutableMap.Builder<Flag, Field> flagsBuilder = ImmutableMap.builder();
    for (Class flags : flagClasses) {
      for (Field field : flags.getDeclaredFields()) {
        if (!field.isAnnotationPresent(Flag.class)) {
          continue;
        }
        Flag annotation = field.getAnnotation(Flag.class);
        flagsBuilder.put(annotation, field);
        OptionBuilder.withDescription(annotation.description());
        OptionBuilder.withLongOpt(annotation.value());
        if (annotation.multiple()) {
          OptionBuilder.hasArgs();
        } else if (boolean.class.equals(field.getType())) {
          OptionBuilder.hasArg(false);
        } else {
          OptionBuilder.hasArg();
          OptionBuilder.withArgName(annotation.value());
        }
        options.addOption(OptionBuilder.create(annotation.value()));
      }
    }
    return new FlagSpec(options, flagsBuilder.build());
  }

}
