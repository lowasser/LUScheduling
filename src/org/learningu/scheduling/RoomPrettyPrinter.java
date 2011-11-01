package org.learningu.scheduling;

import java.io.File;
import java.util.logging.Logger;

import org.learningu.scheduling.flags.Flag;
import org.learningu.scheduling.pretty.Csv;
import org.learningu.scheduling.pretty.PrettySchedulePrinters;
import org.learningu.scheduling.schedule.Schedule;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.google.inject.Inject;

final class RoomPrettyPrinter extends BasicFutureCallback<Schedule> {
  @Flag(name = "roomScheduleOutput")
  private Optional<File> outputFile;

  @Inject
  RoomPrettyPrinter(Logger logger, Optional<File> outputFile) {
    super(logger);
    this.outputFile = outputFile;
  }

  @Override
  public void process(Schedule value) throws Exception {
    if (outputFile.isPresent()) {
      Csv csv = PrettySchedulePrinters.buildRoomScheduleCsv(value);
      Files.write(csv.toString(), outputFile.get(), Charsets.UTF_8);
    }
  }
}
