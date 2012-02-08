package org.learningu.scheduling;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.learningu.scheduling.graph.SerialGraph.SerialBuilding;
import org.learningu.scheduling.graph.SerialGraph.SerialProgram;
import org.learningu.scheduling.graph.SerialGraph.SerialRoom;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;
import org.learningu.scheduling.schedule.SerialSchedules.SerialStartAssignment;

import edu.uchicago.lowasser.flaginjection.Flag;
import edu.uchicago.lowasser.flaginjection.Flags;

/**
 * Converts a schedule in the CSV format used for exchanging data with the LU website, to the
 * protocol buffer format for schedules.
 * 
 * @author lowasser
 */
public class CsvScheduleToProto {
  private Logger logger;
  private final File csvFile;
  private final File outputProtoFile;
  private final File programFile;

  @Inject
  CsvScheduleToProto(
      @Flag(name = "csvFile", description = "CSV file describing the current schedule.") File csvFile,
      @Flag(name = "outputProtoFile", description = "Destination of schedule protobuf") File outputProtoFile,
      @Flag(name = "programFile", description = "Protobuf file describing the LU program.") File programFile,
      Logger logger) {
    this.csvFile = csvFile;
    this.outputProtoFile = outputProtoFile;
    this.programFile = programFile;
    this.logger = logger;
  }

  private static final Ordering<SerialStartAssignment> PERIOD_ORDERING =
      new Ordering<SerialStartAssignment>() {
        @Override
        public int compare(SerialStartAssignment left, SerialStartAssignment right) {
          return Ints.compare(left.getPeriodId(), right.getPeriodId());
        }
      }.nullsLast();

  public SerialProgram readProgram() throws IOException {
    return AutoschedulerDataSource.readMessage(SerialProgram.newBuilder(), programFile).build();
  }

  static final class CsvProcessor implements LineProcessor<SerialSchedule> {
    private final SerialProgram program;
    private final Map<String, Integer> roomNameToId;
    private final Map<Integer, SerialStartAssignment> firstAssignment;
    private final Logger logger;

    @Inject
    CsvProcessor(SerialProgram program) {
      this.program = program;
      this.firstAssignment = Maps.newHashMap();
      this.logger = Logger.getLogger("org.learningu.scheduling.CsvScheduleToProto.CsvProcessor");
      ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
      for (SerialBuilding building : program.getBuildingList()) {
        for (SerialRoom room : building.getRoomList()) {
          builder.put(room.getName(), room.getRoomId());
        }
      }
      this.roomNameToId = builder.build();
    }

    @Override
    public boolean processLine(String line) throws IOException {
      List<String> cells = ImmutableList.copyOf(Splitter.on(',').trimResults().split(line));
      int sectionId = Integer.parseInt(cells.get(0));
      Integer roomId = roomNameToId.get(cells.get(1));
      if (roomId == null) {
        logger.severe("Could not locate room with name \"" + cells.get(1)
            + "\".  Skipping this entry.");
        return true;
      }
      int periodId = Integer.parseInt(cells.get(2));
      SerialStartAssignment assign =
          SerialStartAssignment
              .newBuilder()
              .setSectionId(sectionId)
              .setRoomId(roomId)
              .setPeriodId(periodId)
              .build();
      SerialStartAssignment prev = firstAssignment.get(sectionId);
      firstAssignment.put(sectionId, PERIOD_ORDERING.min(assign, prev));
      return true;
    }

    @Override
    public SerialSchedule getResult() {
      return SerialSchedule.newBuilder().addAllAssignment(firstAssignment.values()).build();
    }
  }

  public void translateSchedule() throws IOException {
    logger.info("Reading program proto.");
    SerialProgram program = readProgram();
    logger.info("Reading CSV schedule " + csvFile);
    SerialSchedule schedule = Files.readLines(csvFile, Charsets.UTF_8, new CsvProcessor(program));
    logger.info("Schedule read.  Outputting protocol buffer.");
    OutputStream stream = new FileOutputStream(outputProtoFile);
    try {
      schedule.writeTo(stream);
    } finally {
      logger.info("Closing down output stream.");
      stream.close();
    }
  }

  public static void main(String[] args) throws IOException {
    Injector injector =
        Flags.bootstrapFlagInjector(args, Flags.flagBindings(CsvScheduleToProto.class));
    injector.getInstance(CsvScheduleToProto.class).translateSchedule();
  }
}
