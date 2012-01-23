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
import java.util.logging.Level;
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
  @Flag(name = "optimizationSpecFile",
      description = "File specifying the configuration of the optimizer.")
  private File optimizationSpecFile;

  @Inject
  @Flag(name = "initialScheduleFile",
      description = "File containing an initial schedule to start from.  Optional.")
  private Optional<File> initialScheduleFile;

  @Inject
  @Flag(name = "logicFile",
      description = "File specifying what schedule logic to use in validation")
  private File logicFile;

  private final Logger logger;

  @Inject
  AutoschedulerDataSource(Logger logger) {
    this.logger = logger;
  }

  private void logReading(String readTarget, File file) {
    logger.log(Level.FINE, "Reading in {0} from {1}", new Object[] { readTarget, file });
  }

  private void logReadComponents(String type, int count) {
    logger.log(Level.FINE, "Read {0} {1} components", new Object[] { count, type });
  }

  public SerialSchedule getSerialSchedule() throws IOException {
    if (initialScheduleFile.isPresent()) {
      logReading("schedule", initialScheduleFile.get());
      return readMessage(SerialSchedule.newBuilder(), initialScheduleFile.get()).build();
    } else {
      logger.fine("No initial schedule specified; starting with an empty schedule.");
      return SerialSchedule.newBuilder().build();
    }
  }

  public SerialLogics getSerialLogics() throws IOException {
    logReading("schedule logic", logicFile);
    SerialLogics logics = readMessage(SerialLogics.newBuilder(), logicFile).build();
    logReadComponents("logic", logics.getLogicCount());
    return logics;
  }

  public SerialProgram getSerialProgram() throws IOException {
    logReading("program specification", programFile);
    SerialProgram program = readMessage(SerialProgram.newBuilder(), programFile).build();
    logReadComponents("building", program.getBuildingCount());
    logReadComponents("resource", program.getResourceCount());
    logReadComponents("subject", program.getSubjectCount());
    logReadComponents("section", program.getSectionCount());
    logReadComponents("teacher", program.getTeacherCount());
    logReadComponents("time block", program.getTimeBlockCount());
    return program;
  }

  public OptimizerSpec getOptimizerSpec() throws IOException {
    logReading("optimizer spec", optimizationSpecFile);
    OptimizerSpec spec = readMessage(OptimizerSpec.newBuilder(), optimizationSpecFile).build();
    logReadComponents("scorer", spec.getScorer().getComponentCount());
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

  static <T extends Message.Builder> T readMessage(T builder, File file) throws IOException {
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
