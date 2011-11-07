package org.learningu.scheduling.json;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.learningu.scheduling.annotations.Initial;
import org.learningu.scheduling.graph.SerialGraph.SerialBuilding;
import org.learningu.scheduling.graph.SerialGraph.SerialPeriod;
import org.learningu.scheduling.graph.SerialGraph.SerialProgram;
import org.learningu.scheduling.graph.SerialGraph.SerialResource;
import org.learningu.scheduling.graph.SerialGraph.SerialRoom;
import org.learningu.scheduling.graph.SerialGraph.SerialSection;
import org.learningu.scheduling.graph.SerialGraph.SerialSubject;
import org.learningu.scheduling.graph.SerialGraph.SerialTeacher;
import org.learningu.scheduling.graph.SerialGraph.SerialTimeBlock;

public class JsonProgramProvider implements Provider<SerialProgram> {
  private final Map<Integer, SerialResource> baseResources;
  private final Map<Integer, SerialTeacher> baseTeachers;
  private final Map<Integer, SerialRoom> baseRooms;
  private final Map<Integer, SerialSection> baseSections;
  private final Map<Integer, SerialPeriod> basePeriods;

  private SerialResource parseResource(JsonObject obj) {
    SerialResource.Builder builder = SerialResource.newBuilder();
    builder.setResourceId(obj.get("uid").getAsInt());
    builder.setDescription(obj.get("name").getAsString());
    mergeBase(builder, builder.getResourceId(), baseResources);
    return builder.build();
  }

  public <T extends GeneratedMessage.Builder<T>> void mergeBase(
      T builder,
      int id,
      Map<Integer, ? extends Message> base) {
    if (base.containsKey(id)) {
      builder.mergeFrom(base.remove(id));
    }
  }

  public SerialPeriod parsePeriod(JsonObject obj) {
    SerialPeriod.Builder builder = SerialPeriod.newBuilder();
    builder.setPeriodId(obj.get("id").getAsInt());
    builder.setDescription(obj.get("description").getAsString());
    builder.setShortDescription(obj.get("short_description").getAsString());
    mergeBase(builder, builder.getPeriodId(), basePeriods);
    return builder.build();
  }

  private SerialTeacher parseTeacher(JsonObject obj) {
    SerialTeacher.Builder builder = SerialTeacher.newBuilder();
    builder.setTeacherId(obj.get("uid").getAsInt());
    builder.setName(obj.get("text").getAsString());
    for (JsonElement avail : obj.get("availability").getAsJsonArray()) {
      builder.addAvailablePeriod(avail.getAsInt());
    }
    mergeBase(builder, builder.getTeacherId(), baseTeachers);
    List<Integer> periodList = builder.getAvailablePeriodList();
    builder.clearAvailablePeriod();
    builder.addAllAvailablePeriod(ImmutableSortedSet.copyOf(periodList));
    return builder.build();
  }

  private final AtomicInteger roomId = new AtomicInteger();

  private SerialRoom parseRoom(JsonObject obj) {
    SerialRoom.Builder builder = SerialRoom.newBuilder();
    builder.setName(obj.get("text").getAsString());
    builder.setRoomId(roomId.getAndIncrement());
    builder.setCapacity(obj.get("num_students").getAsInt());
    for (JsonElement res : obj.get("associated_resources").getAsJsonArray()) {
      builder.addResource(res.getAsInt());
    }
    for (JsonElement pd : obj.get("availability").getAsJsonArray()) {
      builder.addAvailablePeriod(pd.getAsInt());
    }
    mergeBase(builder, builder.getRoomId(), baseRooms);
    List<Integer> availablePeriodList = builder.getAvailablePeriodList();
    builder.clearAvailablePeriod();
    builder.addAllAvailablePeriod(ImmutableSortedSet.copyOf(availablePeriodList));
    List<Integer> resourceList = builder.getResourceList();
    builder.clearResource();
    builder.addAllResource(ImmutableSortedSet.copyOf(resourceList));
    return builder.build();
  }

  private SerialSection parseSection(JsonObject obj) {
    SerialSection.Builder builder = SerialSection.newBuilder();
    builder.setSectionId(obj.get("id").getAsInt());

    Integer subjId = subjects.get(obj.get("category").getAsString());
    if (subjId == null) {
      subjects.put(obj.get("category").getAsString(), subjId = subjects.size());
    }
    builder.setSubjectId(subjId);

    JsonArray gradeArray = obj.get("grades").getAsJsonArray();
    builder.setMinGrade(gradeArray.get(0).getAsInt());
    builder.setMaxGrade(gradeArray.get(1).getAsInt());

    builder.setCourseTitle(obj.get("emailcode").getAsString() + ": "
        + obj.get("text").getAsString());
    int size = obj.get("class_size_max").getAsInt();
    builder.setEstimatedClassSize(size).setMaxClassSize(size);
    for (JsonElement t : obj.get("teachers").getAsJsonArray()) {
      builder.addTeacherId(t.getAsInt());
    }
    builder.setCourseId(obj.get("class_id").getAsInt());
    builder.setPeriodLength(DoubleMath.roundToInt(
        obj.get("length").getAsDouble(),
        RoundingMode.HALF_EVEN));
    mergeBase(builder, builder.getCourseId(), baseSections);
    List<Integer> teacherIdList = builder.getTeacherIdList();
    builder.clearTeacherId();
    builder.addAllTeacherId(ImmutableSortedSet.copyOf(teacherIdList));
    return builder.build();
  }

  private final JsonArray teachers;
  private final JsonArray periods;
  private final JsonArray resources;
  private final JsonArray sections;
  private final JsonArray rooms;
  private final Map<String, Integer> subjects = Maps.newHashMap();

  @Inject
  JsonProgramProvider(
      @Initial SerialProgram initial,
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
    ImmutableMap.Builder<Integer, SerialResource> resourceBuilder = ImmutableMap.builder();
    for (SerialResource r : initial.getResourceList()) {
      resourceBuilder.put(r.getResourceId(), r);
    }
    this.baseResources = Maps.newHashMap(resourceBuilder.build());
    ImmutableMap.Builder<Integer, SerialPeriod> periodBuilder = ImmutableMap.builder();
    for (SerialTimeBlock b : initial.getTimeBlockList()) {
      for (SerialPeriod p : b.getPeriodList()) {
        periodBuilder.put(p.getPeriodId(), p);
      }
    }
    this.basePeriods = Maps.newHashMap(periodBuilder.build());
    ImmutableMap.Builder<Integer, SerialTeacher> teacherBuilder = ImmutableMap.builder();
    for (SerialTeacher t : initial.getTeacherList()) {
      teacherBuilder.put(t.getTeacherId(), t);
    }
    this.baseTeachers = Maps.newHashMap(teacherBuilder.build());
    ImmutableMap.Builder<Integer, SerialRoom> roomBuilder = ImmutableMap.builder();
    for (SerialBuilding b : initial.getBuildingList()) {
      for (SerialRoom r : b.getRoomList()) {
        roomBuilder.put(r.getRoomId(), r);
      }
    }
    this.baseRooms = Maps.newHashMap(roomBuilder.build());
    ImmutableMap.Builder<Integer, SerialSection> sectionBuilder = ImmutableMap.builder();
    for (SerialSection s : initial.getSectionList()) {
      sectionBuilder.put(s.getSectionId(), s);
    }
    this.baseSections = Maps.newHashMap(sectionBuilder.build());
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
      builder.addResource(parseResource(resource.getAsJsonObject()));
    }
    for (JsonElement section : sections) {
      builder.addSection(parseSection(section.getAsJsonObject()));
    }
    ListMultimap<String, SerialRoom> buildings = ArrayListMultimap.create();
    for (JsonElement elem : rooms) {
      SerialRoom room = parseRoom(elem.getAsJsonObject());
      String buildingName = Splitter
          .on(CharMatcher.anyOf(" -"))
          .split(room.getName())
          .iterator()
          .next();
      buildings.put(buildingName, room);
    }
    int buildingId = 0;
    for (Map.Entry<String, Collection<SerialRoom>> building : buildings.asMap().entrySet()) {
      SerialBuilding.Builder bBuilder = SerialBuilding.newBuilder();
      bBuilder.setBuildingId(buildingId++);
      bBuilder.setName(building.getKey());
      bBuilder.addAllRoom(building.getValue());
      builder.addBuilding(bBuilder);
    }
    for (Entry<String, Integer> entry : subjects.entrySet()) {
      builder.addSubject(SerialSubject
          .newBuilder()
          .setSubjectId(entry.getValue())
          .setTitle(entry.getKey()));
    }
    for (SerialResource res : baseResources.values()) {
      builder.addResource(res);
    }
    return builder.build();
  }

}
