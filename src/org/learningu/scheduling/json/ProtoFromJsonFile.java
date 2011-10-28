package org.learningu.scheduling.json;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.protobuf.TextFormat;

import java.io.File;
import java.io.IOException;

import org.learningu.scheduling.FlagsModule;
import org.learningu.scheduling.OptionsModule;
import org.learningu.scheduling.annotations.Flag;
import org.learningu.scheduling.graph.SerialGraph.SerialProgram;

public final class ProtoFromJsonFile {
  @Flag("teachers")
  private final File teachersFile;
  @Flag("periods")
  private final File periodsFile;
  @Flag("rooms")
  private final File roomsFile;
  @Flag("sections")
  private final File sectionsFile;
  @Flag("resources")
  private final File resourcesFile;
  @Flag("output")
  private final File outputFile;

  @Inject
  ProtoFromJsonFile(
      @Named("teachers") File teachersFile,
      @Named("periods") File periodsFile,
      @Named("sections") File sectionsFile,
      @Named("resources") File resourcesFile,
      @Named("rooms") File roomsFile,
      @Named("output") File outputFile) {
    this.teachersFile = teachersFile;
    this.periodsFile = periodsFile;
    this.roomsFile = roomsFile;
    this.sectionsFile = sectionsFile;
    this.resourcesFile = resourcesFile;
    this.outputFile = outputFile;
  }

  public static void main(final String[] args) throws JsonSyntaxException, IOException {
    Injector configuredInjector = OptionsModule.buildOptionsInjector(
        args,
        FlagsModule.create(ProtoFromJsonFile.class));
    ProtoFromJsonFile io = configuredInjector.getInstance(ProtoFromJsonFile.class);
    final JsonArray teachers = io.getTeachers();
    final JsonArray periods = io.getPeriods();
    final JsonArray sections = io.getSections();
    final JsonArray resources = io.getResources();
    final JsonArray rooms = io.getRooms();
    Injector jsonInjector = configuredInjector.createChildInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(JsonArray.class).annotatedWith(Names.named("json_teachers")).toInstance(teachers);
        bind(JsonArray.class).annotatedWith(Names.named("json_periods")).toInstance(periods);
        bind(JsonArray.class).annotatedWith(Names.named("json_sections")).toInstance(sections);
        bind(JsonArray.class).annotatedWith(Names.named("json_resources")).toInstance(resources);
        bind(JsonArray.class).annotatedWith(Names.named("json_rooms")).toInstance(rooms);
        bind(SerialProgram.class).toProvider(JsonProgramProvider.class).in(Scopes.SINGLETON);
      }
    });
    SerialProgram program = jsonInjector.getInstance(SerialProgram.class);
    Files.write(TextFormat.printToString(program), io.outputFile, Charsets.UTF_8);
  }

  public JsonArray getTeachers() throws JsonSyntaxException, IOException {
    return new JsonParser().parse(Files.toString(teachersFile, Charsets.UTF_8)).getAsJsonArray();
  }

  public JsonArray getPeriods() throws JsonSyntaxException, IOException {
    return new JsonParser().parse(Files.toString(periodsFile, Charsets.UTF_8)).getAsJsonArray();
  }

  public JsonArray getRooms() throws JsonSyntaxException, IOException {
    return new JsonParser().parse(Files.toString(roomsFile, Charsets.UTF_8)).getAsJsonArray();
  }

  public JsonArray getSections() throws JsonSyntaxException, IOException {
    return new JsonParser().parse(Files.toString(sectionsFile, Charsets.UTF_8)).getAsJsonArray();
  }

  public JsonArray getResources() throws JsonSyntaxException, IOException {
    return new JsonParser().parse(Files.toString(resourcesFile, Charsets.UTF_8)).getAsJsonArray();
  }
}
