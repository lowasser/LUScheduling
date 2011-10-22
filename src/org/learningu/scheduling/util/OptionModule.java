package org.learningu.scheduling.util;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.google.inject.AbstractModule;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * A Guice module that reads command-line arguments based on the annotated elements of the
 * specified classes, and injects the values specified on the command line.
 * 
 * If flags are missing, this module simply won't inject them. You can use this module to override
 * Guice modules with the default values.
 * 
 * @author lowasser
 */
public class OptionModule extends AbstractModule {
  public static OptionModule createOptionModule(String[] args, Class<?>... flagSettings)
      throws ParseException {
    return new OptionModule(args, flagSettings);
  }

  private final Map<Flag, Field> flags;
  private final CommandLine commandLine;

  private OptionModule(String[] args, Class<?>... flagSettings) throws ParseException {
    Options options = new Options();
    ImmutableMap.Builder<Flag, Field> flagsBuilder = ImmutableMap.builder();

    for (Class<?> flags : flagSettings) {
      for (Field field : flags.getDeclaredFields()) {
        if (!field.isAnnotationPresent(Flag.class)) {
          continue;
        }
        Flag annotation = field.getAnnotation(Flag.class);
        flagsBuilder.put(annotation, field);
        OptionBuilder.withDescription(annotation.description());
        OptionBuilder.withLongOpt(annotation.value());
        if (boolean.class.equals(field.getType())) {
          OptionBuilder.hasArg(false);
        } else {
          OptionBuilder.hasArg();
          OptionBuilder.withArgName(annotation.value());
        }
        options.addOption(OptionBuilder.create(annotation.value()));
      }
    }

    this.flags = flagsBuilder.build();

    CommandLineParser parser = new PosixParser();
    this.commandLine = parser.parse(options, args);
  }

  @Override
  protected void configure() {
    for (Map.Entry<Flag, Field> entry : flags.entrySet()) {
      Class<?> type = entry.getValue().getType();
      Flag flag = entry.getKey();
      String argument;
      argument = commandLine.getOptionValue(flag.value());
      if (argument == null && !flag.defaultValue().isEmpty()) {
        argument = flag.defaultValue();
      }

      Named annotation = Names.named(flag.value());
      if (argument == null) {
        if (boolean.class.equals(type)) {
          bindConstant().annotatedWith(annotation).to(commandLine.hasOption(flag.value()));
        } else {
          continue;
        }
      } else if (type.isPrimitive() || type.isEnum() || Primitives.isWrapperType(type)) {
        bindConstant().annotatedWith(annotation).to(argument);
      } else if (BigInteger.class.equals(type)) {
        bind(BigInteger.class).annotatedWith(annotation).toInstance(
            argument.isEmpty() ? BigInteger.ZERO : new BigInteger(argument));
      } else if (String.class.equals(type)) {
        bind(String.class).annotatedWith(annotation).toInstance(argument);
      } else if (Period.class.equals(type)) {
        bind(Period.class).annotatedWith(annotation).toInstance(
            argument.isEmpty() ? Period.ZERO : Period.parse(argument, PERIOD_FORMATTER));
      } else if (File.class.equals(type)) {
        bind(File.class).annotatedWith(annotation).toInstance(new File(argument));
      } else {
        throw new IllegalStateException("Don't know what to do with option type " + type);
      }
    }
  }

  private static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder().printZeroNever()
      .appendDays()
      .appendSuffix("d")
      .appendHours()
      .appendSuffix("h")
      .appendMinutes()
      .appendSuffix("m")
      .appendSeconds()
      .appendSuffix("s")
      .appendMillis()
      .appendSuffix("ms")
      .toFormatter();
}
