package org.learningu.scheduling.flags;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.util.Modules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Framework for pulling together the fields annotated with @Flag from a collection of classes, and
 * bootstrapping a Guice module that will provide the flag values at runtime.
 * 
 * <p>
 * The bootstrapping process for flags works like this: you construct modules using
 * {@link #flagBindings} that announce to Guice all of the @Flag-annotated fields in the specified
 * classes. Calling {@link #bootstrapFlagInjector(String[], Module...)} with these modules and
 * possibly others, Guice first creates an injector capable of generating the {@link Options}
 * object that encapsulates all the flags requested. This base injector, in combination with the
 * specified command line arguments, is used to construct (via injection!) a module that binds all
 * the desired flags to their values. This module is used to create a bootstrapped injector that
 * properly injects flag values to the desired locations.
 * 
 * @author lowasser
 */
public final class Flags {
  private Flags() {
  }

  public static void addFlagBinding(
      Binder binder,
      final Flag flagAnnotation,
      final TypeLiteral<?> parameterType) {
    MapBinder
        .newMapBinder(binder, Flag.class, Type.class)
        .addBinding(flagAnnotation)
        .toInstance(parameterType.getType());
  }

  public static Module flagBindings(final Class<?>... classes) {
    return new AbstractModule() {
      @Override
      protected void configure() {
        for (Class<?> clazz : classes) {
          addFlagBindings(binder(), TypeLiteral.get(clazz));
        }
      }
    };
  }

  public static void addFlagBindings(Binder binder, TypeLiteral<?> literal) {
    for (Field field : literal.getRawType().getDeclaredFields()) {
      if (field.isAnnotationPresent(Flag.class)) {
        Flag annot = field.getAnnotation(Flag.class);
        addFlagBinding(binder, annot, literal.getFieldType(field));
      }
    }
    for (Constructor<?> constructor : literal.getRawType().getDeclaredConstructors()) {
      List<TypeLiteral<?>> parameterTypes = literal.getParameterTypes(constructor);
      Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
      for (int i = 0; i < parameterTypes.size(); i++) {
        Annotation[] annotations = parameterAnnotations[i];
        TypeLiteral<?> typ = parameterTypes.get(i);
        for (Annotation annot : annotations) {
          if (annot instanceof Flag) {
            addFlagBinding(binder, (Flag) annot, typ);
          }
        }
      }
    }
  }

  public static Injector bootstrapFlagInjector(final String[] args, Module... baseModules) {
    AbstractModule linkingModule = new AbstractModule() {

      @Override
      protected void configure() {
      }

      @SuppressWarnings("unused")
      @Provides
      @RuntimeArguments
      String[] commandLineArguments() {
        return args;
      }

      @SuppressWarnings("unused")
      @Provides
      @Singleton
      Options options(Map<Flag, Type> flagsMap) {
        Options options = new Options();
        for (Flag flag : flagsMap.keySet()) {
          OptionBuilder.hasArgs();
          OptionBuilder.withLongOpt(flag.name());
          OptionBuilder.withDescription(flag.description());
          OptionBuilder.withArgName(flagsMap.get(flag).toString());
          options.addOption(OptionBuilder.create());
        }
        return options;
      }

      @SuppressWarnings("unused")
      @Provides
      @Singleton
      CommandLine commandLine(Options options, @RuntimeArguments String[] args) {
        try {
          return new PosixParser().parse(options, args);
        } catch (ParseException e) {
          throw Throwables.propagate(e);
        }
      }
    };
    Injector baseInjector = Guice.createInjector(Modules.combine(Iterables.concat(
        Arrays.asList(baseModules),
        ImmutableList.of(linkingModule))));
    return baseInjector.createChildInjector(baseInjector.getInstance(FlagBootstrapModule.class));
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static final class FlagBootstrapModule extends AbstractModule {
    private final CommandLine commandLine;
    private final Map<Flag, Type> flagsMap;
    private final Options options;
    private final String main;

    @SuppressWarnings("unused")
    // injected
    @Inject
    FlagBootstrapModule(
        CommandLine commandLine,
        Map<Flag, Type> flagsMap,
        Options options,
        @Named("main") String main) {
      this.commandLine = commandLine;
      this.flagsMap = flagsMap;
      this.options = options;
      this.main = main;
    }

    @Override
    protected void configure() {
      for (Map.Entry<Flag, Type> entry : flagsMap.entrySet()) {
        Flag flagAnnotation = entry.getKey();
        final TypeLiteral literal = TypeLiteral.get(entry.getValue());

        @Nullable
        final String value = commandLine.getOptionValue(flagAnnotation.name());

        try {
          Object result = Converters.converterFor(literal).parse(value);

          bind(literal).annotatedWith(flagAnnotation).toInstance(result);
        } catch (RuntimeException e) {
          if (!flagAnnotation.optional()) {
            new HelpFormatter().printHelp(130, main, "", options, "");
            throw Throwables.propagate(new ParseException("Missing flag: " + flagAnnotation));
          }
        }
      }
    }
  }
}
