package org.learningu.scheduling;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.Pass.OptimizerSpec;
import org.learningu.scheduling.annotations.Flag;
import org.learningu.scheduling.annotations.Initial;
import org.learningu.scheduling.flags.OptionsModule;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.SerialGraph.SerialProgram;
import org.learningu.scheduling.logic.SerialLogic.SerialLogics;
import org.learningu.scheduling.modules.AutoschedulingBaseModule;
import org.learningu.scheduling.modules.AutoschedulingConfigModule;
import org.learningu.scheduling.optimization.Optimizer;
import org.learningu.scheduling.pretty.PrettySchedulePrinters;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.Schedules;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;

public final class Autoscheduling {
  @Flag("programFile")
  private final File programFile;

  @Flag("optimizationSpecFile")
  private final File optimizationSpecFile;

  @Flag(value = "initialScheduleFile", defaultValue = " ")
  private final File initialScheduleFile;

  @Flag(value = "resultScheduleFile", defaultValue = " ")
  private final File resultScheduleFile;

  @Flag(value = "logicFile")
  private final File logicFile;

  @Flag(value = "outputFormat", defaultValue = "TEXT")
  private final MessageOutputFormat outputFormat;

  @Flag(value = "iterations", defaultValue = "1000")
  private final int iterations;

  @Flag(value = "teacherScheduleOutput")
  private final File teacherScheduleOutput;

  @Flag(value = "roomScheduleOutput")
  private final File roomScheduleOutput;

  private final Logger logger;

  enum MessageOutputFormat {
    TEXT {
      @Override
      public void output(OutputStream stream, Message message) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.append(TextFormat.printToString(message));
        writer.close();
      }
    },
    PROTO {
      @Override
      public void output(OutputStream stream, Message message) throws IOException {
        message.writeTo(stream);
      }
    };
    public abstract void output(OutputStream stream, Message message) throws IOException;
  }

  @Inject
  Autoscheduling(
      @Named("programFile") File programFile,
      @Named("optimizationSpecFile") File optimizationSpecFile,
      @Named("initialScheduleFile") File initialScheduleFile,
      @Named("resultScheduleFile") File resultScheduleFile,
      @Named("logicFile") File logicFile,
      @Named("outputFormat") MessageOutputFormat outputFormat,
      @Named("iterations") int iterations,
      @Named("teacherScheduleOutput") File teacherScheduleOutput,
      @Named("roomScheduleOutput") File roomScheduleOutput,
      Logger logger) {
    this.programFile = programFile;
    this.optimizationSpecFile = optimizationSpecFile;
    this.initialScheduleFile = initialScheduleFile;
    this.resultScheduleFile = resultScheduleFile;
    this.logicFile = logicFile;
    this.outputFormat = outputFormat;
    this.iterations = iterations;
    this.teacherScheduleOutput = teacherScheduleOutput;
    this.roomScheduleOutput = roomScheduleOutput;
    this.logger = logger;
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {
    // First, initialize the very basic, completely run-independent bindings.
    Injector flaggedInjector = OptionsModule.buildOptionsInjector(
        args,
        new AutoschedulingBaseModule());
    // We now have enough to initialize the Autoscheduling runner with files and the like.
    Autoscheduling auto = flaggedInjector.getInstance(Autoscheduling.class);
    // Perform the necessary file I/O here, since it's evil to do that from inside providers.
    final SerialProgram serialProgram = auto.getSerialProgram();
    final OptimizerSpec optimizerSpec = auto.getOptimizerSpec();
    final SerialLogics serialLogics = auto.getSerialLogics();
    final SerialSchedule serialSchedule = auto.getSerialSchedule();
    Injector completeInjector = flaggedInjector.createChildInjector(
        new AutoschedulingConfigModule(),
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(SerialLogics.class).toInstance(serialLogics);
            bind(SerialProgram.class).toInstance(serialProgram);
            bind(OptimizerSpec.class).toInstance(optimizerSpec);
            bind(SerialSchedule.class).toInstance(serialSchedule);
          }
        });
    final Schedule initSchedule = completeInjector.getInstance(Key.get(
        Schedule.class,
        Initial.class));
    final Optimizer<Schedule> optimizer = completeInjector.getInstance(Key
        .get(new TypeLiteral<Optimizer<Schedule>>() {}));
    Schedule optSchedule = optimizer.iterate(auto.getIterations(), initSchedule);
    OutputStream outStream = auto.resultScheduleFile.getPath().equals(" ") ? System.out
        : new FileOutputStream(auto.resultScheduleFile);
    auto.outputFormat.output(outStream, Schedules.serialize(optSchedule));
    outStream.close();
    auto.logProgramStats(optSchedule.getProgram());
    auto.logScheduleStats(optSchedule);
    ImmutableSet<Section> unscheduled = ImmutableSet.copyOf(Sets.difference(optSchedule
        .getProgram()
        .getSections(), optSchedule.getScheduledSections()));
    if (!unscheduled.isEmpty()) {
      auto.logger.warning("The following sections were not scheduled: " + unscheduled);
    }
    if (!auto.teacherScheduleOutput.getPath().equals(" ")) {
      Files.write(
          PrettySchedulePrinters.buildTeacherScheduleCsv(optSchedule).toString(),
          auto.teacherScheduleOutput,
          Charsets.UTF_8);
    }
    if (!auto.roomScheduleOutput.getPath().equals(" ")) {
      Files.write(
          PrettySchedulePrinters.buildRoomScheduleCsv(optSchedule).toString(),
          auto.roomScheduleOutput,
          Charsets.UTF_8);
    }
    completeInjector.getInstance(ExecutorService.class).shutdown();
  }

  public int getIterations() {
    return iterations;
  }

  public void logProgramStats(Program program) {
    logger.log(Level.INFO, "Program has {0} sections", program.getSections().size());
  }

  public void logScheduleStats(Schedule schedule) {
    logger.log(Level.INFO, "Schedule scheduled {0} sections", schedule
        .getScheduledSections()
        .size());
    int classHours = 0;
    for (Section section : schedule.getScheduledSections()) {
      classHours += section.getPeriodLength();
    }
    logger.log(Level.INFO, "Schedule scheduled {0} class hours", classHours);
  }

  public SerialSchedule getSerialSchedule() throws IOException {
    if (initialScheduleFile.getPath().equals(" ")) {
      return SerialSchedule.newBuilder().build();
    } else {
      return readMessage(SerialSchedule.newBuilder(), initialScheduleFile).build();
    }
  }

  public SerialLogics getSerialLogics() throws IOException {
    return readMessage(SerialLogics.newBuilder(), logicFile).build();
  }

  public SerialProgram getSerialProgram() throws IOException {
    return readMessage(SerialProgram.newBuilder(), programFile).build();
  }

  public OptimizerSpec getOptimizerSpec() throws IOException {
    return readMessage(OptimizerSpec.newBuilder(), optimizationSpecFile).build();
  }

  private static <T extends Message.Builder> T readMessage(T builder, File file)
      throws IOException {
    FileReader fileReader = new FileReader(file);
    try {
      TextFormat.merge(fileReader, builder);
      return builder;
    } catch (IOException e) {
      // retry as a serialized protobuf
    } finally {
      fileReader.close();
    }
    FileInputStream fileStream = new FileInputStream(file);
    try {
      builder.mergeFrom(fileStream);
      return builder;
    } finally {
      fileStream.close();
    }
  }
}
