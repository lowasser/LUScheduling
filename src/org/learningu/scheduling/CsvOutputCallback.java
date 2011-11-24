package org.learningu.scheduling;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.google.inject.Inject;

import java.io.File;
import java.util.logging.Logger;

import org.learningu.scheduling.flags.Flag;
import org.learningu.scheduling.pretty.Csv;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;

public final class CsvOutputCallback extends BasicFutureCallback<Schedule> {
  @Inject
  @Flag(name = "csvOutput", description = "Schedule output for use in the uploader")
  private Optional<File> csvOutput;

  @Inject
  CsvOutputCallback(Logger logger) {
    super(logger);
  }

  @Override
  public void process(Schedule value) throws Exception {
    if (csvOutput.isPresent()) {
      Csv.Builder builder = Csv.newBuilder();
      for (PresentAssignment assign : value.getPresentAssignments()) {
        builder.add(Csv
            .newRowBuilder()
            .add(assign.getSection().getId())
            .add(assign.getRoom().getName())
            .add(assign.getPeriod().getId())
            .build());
      }
      Files.write(builder.build().toString(), csvOutput.get(), Charsets.UTF_8);
    }
  }
}
