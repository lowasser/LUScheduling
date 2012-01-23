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

/**
 * Object responsible for managing and performing all required file I/O. {@link #buildModule()}
 * performs file I/O and returns a Guice module to provide its results.
 * 
 * @author lowasser
 */
public final class AutoschedulerDataSource {
  @Inject
  @Flag(name = "programFile", description = "File specifying the details of the LU program.")
  private File programFile;

  @Inject
  @Flag(
      name = "optimizationSpecFile",
      description = "File specifying the configuration of the optimizer.")
  private File optimizationSpecFile;

  @Inject
  @Flag(
      name = "initialScheduleFile",
      description = "File containing an initial schedule to start from.  Optional.")
  private Optional<File> initialScheduleFile;

  @Inject
  @Flag(
      name = "logicFile",
      description = "File specifying what schedule logic to use in validation")
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
    SerialLogics logics = readMessage(SerialLogics.newBuilder(), logicFile).build();
    logger.fine("Read " + logics.getLogicCount() + " logic components");
    return logics;
  }

  public SerialProgram getSerialProgram() throws IOException {
    logger.fine("Reading in serialized program specification");
    SerialProgram program = readMessage(SerialProgram.newBuilder(), programFile).build();
    logger.fine("Read " + program.getBuildingCount() + " building components");
    logger.fine("Read " + program.getResourceCount() + " resource components");
    logger.fine("Read " + program.getSubjectCount() + " subject components");
    logger.fine("Read " + program.getTeacherCount() + " teacher components");
    logger.fine("Read " + program.getTimeBlockCount() + " time block components");
    return program;
  }

  public OptimizerSpec getOptimizerSpec() throws IOException {
    logger.fine("Reading in serialized optimizer specification");
    OptimizerSpec spec = readMessage(OptimizerSpec.newBuilder(), optimizationSpecFile).build();
    logger.fine("Read " + spec.getScorer().getComponentCount() + " score components");
    return spec;
  }

  public Module buildModule() throws IOException {
    logger.info("Building data source module");
    final SerialLogics logics = getSerialLogics();
    final SerialProgram program = getSerialProgram();
    final SerialSchedule schedule = getSerialSchedule();
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

  static <T extends Message.Builder> T readMessage(T builder, File file)
      throws IOException {
    FileReader fileReader = new FileReader(file);
    try {
      TextFormat.merge(fileReader, builder);
      return builder;
    } catch (IOException e) {
      e.printStackTrace();
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
