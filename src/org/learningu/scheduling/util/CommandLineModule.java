package org.learningu.scheduling.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
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
public class CommandLineModule extends AbstractModule {
  public static CommandLineModule create(String[] args, Class<?>... flagSettings)
      throws ParseException {
    return new CommandLineModule(args, flagSettings);
  }

  private final Map<Flag, Field> flags;

  private final CommandLine commandLine;

  private CommandLineModule(String[] args, Class<?>... flagSettings) throws ParseException {
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

    this.flags = flagsBuilder.build();

    CommandLineParser parser = new PosixParser();
    this.commandLine = parser.parse(options, args);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
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
      } else if (flag.multiple()) {
        ParameterizedType genericType = (ParameterizedType) entry.getValue().getGenericType();
        Collection<Object> values;
        Collection<Object> returns;
        if (genericType.getRawType().equals(List.class)) {
          values = Lists.newArrayList();
          returns = Collections.unmodifiableList((List<Object>) values);
        } else if (genericType.getRawType().equals(Set.class)) {
          values = Sets.newLinkedHashSet();
          returns = Collections.unmodifiableSet((Set<Object>) values);
        } else {
          throw new IllegalStateException("Don't know what to do with " + genericType);
        }
        Type[] actualTypeArguments = genericType.getActualTypeArguments();
        Type arg = actualTypeArguments[0];
        Iterable<String> splitInput = Splitter
            .on(',')
            .trimResults()
            .omitEmptyStrings()
            .split(argument);
        if (Integer.class.equals(arg)) {
          for (String split : splitInput) {
            values.add(Integer.parseInt(split));
          }
        } else if (Double.class.equals(arg)) {
          for (String split : splitInput) {
            values.add(Double.parseDouble(split));
          }
        } else if (arg instanceof Class && ((Class<?>) arg).isEnum()) {
          for (String split : splitInput) {
            values.add(Enum.valueOf((Class<? extends Enum>) arg, split));
          }
        } else if (String.class.equals(arg)) {
          Iterables.addAll(values, splitInput);
        } else {
          throw new IllegalStateException("Don't know what to do with " + arg);
        }
        bind((Key) Key.get(genericType, annotation)).toInstance(returns);
      } else if (type.isPrimitive() || type.isEnum() || Primitives.isWrapperType(type)) {
        bindConstant().annotatedWith(annotation).to(argument);
      } else if (Level.class.equals(type)) {
        bind(Level.class).annotatedWith(annotation).toInstance(Level.parse(argument));
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

  private static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
      .printZeroNever()
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
