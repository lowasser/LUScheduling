package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.Pass.OptimizerSpec;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.SerialGraph.SerialProgram;
import org.learningu.scheduling.logic.SerialLogic.SerialLogics;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;
import org.learningu.scheduling.schedule.SerialSchedules.SerialStartAssignment;

import edu.uchicago.lowasser.flaginjection.Flag;

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

  private static final Splitter SPLITTER = Splitter.on(',').trimResults(
      CharMatcher.WHITESPACE.or(CharMatcher.is('"')));

  private static class CsvAssignment {
    private final int sectionId;
    private final String roomName;
    private final int timeSlot;

    CsvAssignment(int sectionId, String roomName, int timeSlot) {
      this.sectionId = sectionId;
      this.roomName = roomName;
      this.timeSlot = timeSlot;
    }

    SerialStartAssignment toAssignment(Program program) {
      Section section = program.getSection(sectionId);
      checkNotNull(section, "Could not find section with id %s", sectionId);
      Room room = program.getRoom(roomName);
      checkNotNull(room, "Could not find room with name %s", roomName);
      ClassPeriod period = program.getPeriod(timeSlot);
      checkNotNull(period, "Could not find time period with id %s", timeSlot);
      return SerialStartAssignment
          .newBuilder()
          .setLocked(true)
          .setPeriodId(period.getId())
          .setRoomId(room.getId())
          .setSectionId(section.getId())
          .build();
    }
  }

  public List<CsvAssignment> getSerialSchedule(SerialProgram program) throws IOException {
    if (initialScheduleFile.isPresent()) {
      logReading("schedule", initialScheduleFile.get());
      List<CsvAssignment> assignments = Lists.newArrayList();
      BufferedReader reader = new BufferedReader(new FileReader(initialScheduleFile.get()));
      while (reader.ready()) {
        Iterator<String> line = SPLITTER.split(reader.readLine()).iterator();
        if (!line.hasNext()) {
          continue;
        }
        int sectionId = Integer.parseInt(line.next());
        String roomName = line.next();
        int timeId = Integer.parseInt(line.next());
        assignments.add(new CsvAssignment(sectionId, roomName, timeId));
      }
      return ImmutableList.copyOf(assignments);
    } else {
      logger.fine("No initial schedule specified; starting with an empty schedule.");
      return ImmutableList.of();
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
    final List<CsvAssignment> schedule = getSerialSchedule(program);
    final OptimizerSpec optSpec = getOptimizerSpec();
    logger.fine("Reading of data complete.");
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(SerialProgram.class).toInstance(program);
        bind(SerialLogics.class).toInstance(logics);
        bind(OptimizerSpec.class).toInstance(optSpec);
      }

      @Singleton
      @Provides
      SerialSchedule schedule(Program program) {
        SerialSchedule.Builder builder = SerialSchedule.newBuilder();
        for (CsvAssignment assign : schedule) {
          try {
            builder.addAssignment(assign.toAssignment(program));
          } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Skipping " + assign + " due to " + e.getMessage());
          }
        }
        return builder.build();
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
