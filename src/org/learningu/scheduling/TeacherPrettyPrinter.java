package org.learningu.scheduling;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.google.inject.Inject;

import java.io.File;
import java.util.logging.Logger;

import org.learningu.scheduling.flags.Flag;
import org.learningu.scheduling.pretty.Csv;
import org.learningu.scheduling.pretty.PrettySchedulePrinters;
import org.learningu.scheduling.schedule.Schedule;

final class TeacherPrettyPrinter extends BasicFutureCallback<Schedule> {
  @Inject
  @Flag(
      name = "teacherScheduleOutput",
      description = "Output file for a pretty formatted CSV containing teacher schedules.  Optional.")
  private Optional<File> outputFile;

  @Inject
  TeacherPrettyPrinter(Logger logger) {
    super(logger);
  }

  @Override
  public void process(Schedule value) throws Exception {
    if (outputFile.isPresent()) {
      Csv csv = PrettySchedulePrinters.buildTeacherScheduleCsv(value);
      Files.write(csv.toString(), outputFile.get(), Charsets.UTF_8);
    }
  }

}
