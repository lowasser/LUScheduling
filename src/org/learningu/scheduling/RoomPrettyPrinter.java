package org.learningu.scheduling;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.google.inject.Inject;

import java.io.File;
import java.util.logging.Logger;

import org.learningu.scheduling.pretty.Csv;
import org.learningu.scheduling.pretty.PrettySchedulePrinters;
import org.learningu.scheduling.schedule.Schedule;

import edu.uchicago.lowasser.flaginjection.Flag;

final class RoomPrettyPrinter extends BasicFutureCallback<Schedule> {
  @Inject
  @Flag(
      name = "roomScheduleOutput",
      description = "Output file for a pretty formatted CSV containing room schedules.  Optional.")
  private Optional<File> outputFile;

  @Inject
  RoomPrettyPrinter(Logger logger) {
    super(logger);
  }

  @Override
  public void process(Schedule value) throws Exception {
    if (outputFile.isPresent()) {
      Csv csv = PrettySchedulePrinters.buildRoomScheduleCsv(value);
      Files.write(csv.toString(), outputFile.get(), Charsets.UTF_8);
    }
  }
}
