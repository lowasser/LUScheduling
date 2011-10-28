package org.learningu.scheduling.json;

import com.google.common.math.DoubleMath;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

import org.learningu.scheduling.graph.SerialGraph.SerialPeriod;
import org.learningu.scheduling.graph.SerialGraph.SerialProgram;
import org.learningu.scheduling.graph.SerialGraph.SerialRoom;
import org.learningu.scheduling.graph.SerialGraph.SerialRoomProperty;
import org.learningu.scheduling.graph.SerialGraph.SerialSection;
import org.learningu.scheduling.graph.SerialGraph.SerialTeacher;
import org.learningu.scheduling.graph.SerialGraph.SerialTimeBlock;

public class JsonProgramProvider implements Provider<SerialProgram> {
  private SerialRoomProperty parseRoomProperty(JsonObject obj) {
    SerialRoomProperty.Builder builder = SerialRoomProperty.newBuilder();
    builder.setPropertyId(obj.get("uid").getAsInt());
    builder.setDescription(obj.get("name").getAsString());
    return builder.build();
  }

  public SerialPeriod parsePeriod(JsonObject obj) {
    SerialPeriod.Builder builder = SerialPeriod.newBuilder();
    builder.setPeriodId(obj.get("id").getAsInt());
    builder.setDescription(obj.get("description").getAsString());
    builder.setShortDescription(obj.get("short_description").getAsString());
    return builder.build();
  }

  private SerialTeacher parseTeacher(JsonObject obj) {
    SerialTeacher.Builder builder = SerialTeacher.newBuilder();
    builder.setTeacherId(obj.get("uid").getAsInt());
    builder.setName(obj.get("text").getAsString());
    for (JsonElement avail : obj.get("availability").getAsJsonArray()) {
      builder.addAvailablePeriod(avail.getAsInt());
    }
    return builder.build();
  }

  private final AtomicInteger roomId = new AtomicInteger();

  private SerialRoom parseRoom(JsonObject obj) {
    SerialRoom.Builder builder = SerialRoom.newBuilder();
    builder.setName(obj.get("text").getAsString());
    builder.setRoomId(roomId.getAndIncrement());
    builder.setCapacity(obj.get("num_students").getAsInt());
    for (JsonElement res : obj.get("associated_resources").getAsJsonArray()) {
      builder.addRoomProperty(res.getAsInt());
    }
    for (JsonElement pd : obj.get("availability").getAsJsonArray()) {
      builder.addAvailablePeriod(pd.getAsInt());
    }
    return builder.build();
  }

  private SerialSection parseSection(JsonObject obj) {
    SerialSection.Builder builder = SerialSection.newBuilder();
    builder.setCourseTitle(obj.get("emailcode").getAsString() + ": "
        + obj.get("text").getAsString());
    builder.setSectionId(obj.get("id").getAsInt());
    int size = obj.get("class_size_max").getAsInt();
    builder.setEstimatedClassSize(size).setMaxClassSize(size);
    for (JsonElement t : obj.get("teachers").getAsJsonArray()) {
      builder.addTeacherIds(t.getAsInt());
    }
    builder.setPeriodLength(DoubleMath.roundToInt(
        obj.get("length").getAsDouble(),
        RoundingMode.HALF_EVEN));
    return builder.build();
  }

  private final JsonArray teachers;
  private final JsonArray periods;
  private final JsonArray resources;
  private final JsonArray sections;
  private final JsonArray rooms;

  @Inject
  JsonProgramProvider(
      @Named("json_rooms") JsonArray rooms,
      @Named("json_teachers") JsonArray teachers,
      @Named("json_periods") JsonArray periods,
      @Named("json_resources") JsonArray resources,
      @Named("json_sections") JsonArray sections) {
    this.teachers = teachers;
    this.periods = periods;
    this.resources = resources;
    this.sections = sections;
    this.rooms = rooms;
  }

  @Override
  public SerialProgram get() {
    SerialProgram.Builder builder = SerialProgram.newBuilder();
    for (JsonElement teacher : teachers) {
      builder.addTeacher(parseTeacher(teacher.getAsJsonObject()));
    }
    SerialTimeBlock.Builder timeBuilder = SerialTimeBlock.newBuilder();
    timeBuilder.setBlockId(0);
    timeBuilder.setDescription("Program");
    for (JsonElement period : periods) {
      timeBuilder.addPeriod(parsePeriod(period.getAsJsonObject()));
    }
    builder.addTimeBlock(timeBuilder);
    for (JsonElement resource : resources) {
      builder.addRoomProperty(parseRoomProperty(resource.getAsJsonObject()));
    }
    for (JsonElement section : sections) {
      builder.addSection(parseSection(section.getAsJsonObject()));
    }
    for (JsonElement room : rooms) {
      builder.addRoom(parseRoom(room.getAsJsonObject()));
    }
    return builder.build();
  }

}
