package org.learningu.scheduling.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.learningu.scheduling.graph.Serial.SerialProgram;
import org.learningu.scheduling.graph.Serial.SerialTimeBlock;

import com.google.inject.Inject;

public final class ProgramParser {
  private final RoomParser roomParser;

  private final TeacherParser teacherParser;

  private final CourseParser courseParser;

  private final PeriodParser periodParser;

  @Inject
  ProgramParser(RoomParser roomParser, TeacherParser teacherParser, CourseParser courseParser,
      PeriodParser periodParser) {
    this.roomParser = roomParser;
    this.teacherParser = teacherParser;
    this.courseParser = courseParser;
    this.periodParser = periodParser;
  }

  public SerialProgram program(JSONArray rooms, JSONArray teachers, JSONArray sections,
      JSONArray periods) throws JSONException {
    SerialProgram.Builder builder = SerialProgram.newBuilder();
    for (int i = 0; i < rooms.length(); i++) {
      builder.addRooms(roomParser.parseJsonToProto(rooms.getJSONObject(i)));
    }
    for (int i = 0; i < teachers.length(); i++) {
      builder.addTeachers(teacherParser.parseJsonToProto(teachers.getJSONObject(i)));
    }
    for (int i = 0; i < sections.length(); i++) {
      builder.addCourses(courseParser.parseJsonToProto(sections.getJSONObject(i)));
    }
    SerialTimeBlock.Builder timeBuilder = SerialTimeBlock.newBuilder();
    timeBuilder.setBlockId(0).setDescription("Program");
    for (int i = 0; i < periods.length(); i++) {
      timeBuilder.addPeriods(periodParser.parseJsonToProto(periods.getJSONObject(i)));
    }
    builder.addTimeBlocks(timeBuilder);
    return builder.build();
  }
}
