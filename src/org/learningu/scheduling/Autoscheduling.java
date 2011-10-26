package org.learningu.scheduling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.learningu.scheduling.Pass.OptimizerSpec;
import org.learningu.scheduling.annotations.Flag;
import org.learningu.scheduling.annotations.Initial;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.SerialGraph.SerialProgram;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

public final class Autoscheduling {
  @Flag("programFile")
  private final File programFile;
  @Flag("optimizationSpecFile")
  private final File optimizationSpecFile;
  @Flag(value = "initialScheduleFile", defaultValue = "")
  private final File initialScheduleFile;

  @Flag(value = "resultScheduleFile", defaultValue = "")
  private final File resultScheduleFile;

  @Flag(value = "outputFormat")
  private final MessageOutputFormat outputFormat;

  enum MessageOutputFormat {
    TEXT {
      @Override
      public void output(File file, Message message) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        try {
          TextFormat.print(message, fileWriter);
        } finally {
          fileWriter.close();
        }
      }
    };
    public abstract void output(File file, Message message) throws IOException;
  }

  @Inject
  Autoscheduling(
      @Named("programFile") File programFile,
      @Named("optimizationSpecFile") File optimizationSpecFile,
      @Named("initialScheduleFile") File initialScheduleFile,
      @Named("resultScheduleFile") File resultScheduleFile,
      @Named("outputFormat") MessageOutputFormat outputFormat) {
    this.programFile = programFile;
    this.optimizationSpecFile = optimizationSpecFile;
    this.initialScheduleFile = initialScheduleFile;
    this.resultScheduleFile = resultScheduleFile;
    this.outputFormat = outputFormat;
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    // First, initialize the very basic, completely run-independent bindings.
    Injector baseInjector = Guice.createInjector(new AutoschedulingBaseModule());
    // Next, inject the flags.
    Injector flaggedInjector =
        baseInjector.createChildInjector(baseInjector.getInstance(OptionsModule.class));
    // We now have enough to initialize the Autoscheduling runner with files and the like.
    Autoscheduling auto = flaggedInjector.getInstance(Autoscheduling.class);
    // Perform the necessary file I/O here, since it's evil to do that from inside providers.
    final SerialProgram serialProgram = auto.getSerialProgram();
    final OptimizerSpec optimizerSpec = auto.getOptimizerSpec();
    final SerialSchedule serialSchedule = auto.getSerialSchedule();
    Injector completeInjector =
        flaggedInjector.createChildInjector(
            new AutoschedulingConfigModule(),
            new PassModule(),
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(SerialProgram.class).toInstance(serialProgram);
                bind(OptimizerSpec.class).toInstance(optimizerSpec);
                bind(SerialSchedule.class).toInstance(serialSchedule);
              }
            });
    final Program initProgram = completeInjector.getInstance(Key.get(Program.class, Initial.class));
    final SerialSchedule initSchedule = 
  }

  public SerialSchedule getSerialSchedule() throws IOException {
    if (initialScheduleFile.getPath().isEmpty()) {
      return SerialSchedule.newBuilder().build();
    } else {
      return readMessage(SerialSchedule.newBuilder(), initialScheduleFile).build();
    }
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
