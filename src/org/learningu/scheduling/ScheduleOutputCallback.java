package org.learningu.scheduling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

import org.learningu.scheduling.flags.Flag;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.Schedules;
import org.learningu.scheduling.schedule.SerialSchedules.SerialSchedule;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

final class ScheduleOutputCallback extends BasicFutureCallback<Schedule> {
  enum MessageOutputFormat {
    TEXT {
      @Override
      public void output(OutputStream stream, Message message) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.append(TextFormat.printToString(message));
      }
    },
    PROTO {
      @Override
      public void output(OutputStream stream, Message message) throws IOException {
        message.writeTo(stream);
      }
    };
    public abstract void output(OutputStream stream, Message message) throws IOException;
  }

  @Flag(name = "out")
  private Optional<File> outputFile;

  @Flag(name = "outputFormat")
  private MessageOutputFormat outputFormat;

  ScheduleOutputCallback(Logger logger) {
    super(logger);
  }

  @Override
  public void process(Schedule value) throws Exception {
    SerialSchedule serial = Schedules.serialize(value);
    if (outputFile.isPresent()) {
      FileOutputStream outStream = new FileOutputStream(outputFile.get());
      try {
        outputFormat.output(outStream, serial);
      } finally {
        outStream.close();
      }
    } else {
      outputFormat.output(System.out, serial);
    }
  }
}
