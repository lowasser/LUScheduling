package org.learningu.scheduling;

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

import org.learningu.scheduling.Pass.OptimizerSpec;
import org.learningu.scheduling.annotations.Flag;
import org.learningu.scheduling.annotations.Initial;
import org.learningu.scheduling.graph.SerialGraph.SerialProgram;
import org.learningu.scheduling.logic.SerialLogic.SerialLogics;
import org.learningu.scheduling.optimization.Optimizer;
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
      @Named("iterations") int iterations) {
    this.programFile = programFile;
    this.optimizationSpecFile = optimizationSpecFile;
    this.initialScheduleFile = initialScheduleFile;
    this.resultScheduleFile = resultScheduleFile;
    this.logicFile = logicFile;
    this.outputFormat = outputFormat;
    this.iterations = iterations;
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
    completeInjector.getInstance(ExecutorService.class).shutdown();
  }

  public int getIterations() {
    return iterations;
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
