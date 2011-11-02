package org.learningu.scheduling;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

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

  @Inject
  @Flag(name = "out")
  private Optional<File> outputFile;

  @Inject(optional = true)
  @Flag(name = "outputFormat", optional = true)
  private MessageOutputFormat outputFormat = MessageOutputFormat.TEXT;

  @Inject
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
