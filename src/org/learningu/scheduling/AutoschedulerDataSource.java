package org.learningu.scheduling;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import org.learningu.scheduling.Pass.OptimizerSpec;
import org.learningu.scheduling.flags.Flag;
import org.learningu.scheduling.graph.SerialGraph.SerialProgram;
import org.learningu.scheduling.logic.SerialLogic.SerialLogics;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;

public final class AutoschedulerDataSource {
  @Inject
  @Flag(name = "programFile")
  private File programFile;

  @Inject
  @Flag(name = "optimizationSpecFile")
  private File optimizationSpecFile;

  @Inject
  @Flag(name = "initialScheduleFile")
  private Optional<File> initialScheduleFile;

  @Inject
  @Flag(name = "logicFile")
  private File logicFile;

  private final Logger logger;

  @Inject
  AutoschedulerDataSource(Logger logger) {
    this.logger = logger;
  }

  public SerialSchedule getSerialSchedule() throws IOException {
    if (initialScheduleFile.isPresent()) {
      logger.fine("Reading in initial schedule.");
      return readMessage(SerialSchedule.newBuilder(), initialScheduleFile.get()).build();
    } else {
      logger.fine("No initial schedule specified; starting with an empty schedule.");
      return SerialSchedule.newBuilder().build();
    }
  }

  public SerialLogics getSerialLogics() throws IOException {
    logger.fine("Reading in schedule validity logic specification");
    return readMessage(SerialLogics.newBuilder(), logicFile).build();
  }

  public SerialProgram getSerialProgram() throws IOException {
    logger.fine("Reading in serialized program specification");
    return readMessage(SerialProgram.newBuilder(), programFile).build();
  }

  public OptimizerSpec getOptimizerSpec() throws IOException {
    logger.fine("Reading in serialized optimizer specification");
    return readMessage(OptimizerSpec.newBuilder(), optimizationSpecFile).build();
  }

  public Module buildModule() throws IOException {
    logger.info("Building data source module");
    final SerialSchedule schedule = getSerialSchedule();
    final SerialLogics logics = getSerialLogics();
    final SerialProgram program = getSerialProgram();
    final OptimizerSpec optSpec = getOptimizerSpec();
    logger.fine("Reading of data complete.");
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(SerialSchedule.class).toInstance(schedule);
        bind(SerialLogics.class).toInstance(logics);
        bind(SerialProgram.class).toInstance(program);
        bind(OptimizerSpec.class).toInstance(optSpec);
      }
    };
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
