package org.learningu.scheduling.logic;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Resource;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

public final class ResourceLogic extends ScheduleLogic {
  /*
   * When many classes request the same floating resource, we pick one randomly to conflict with.
   */
  private final Random rand;

  @Inject
  ResourceLogic(Random rand) {
    this.rand = rand;
  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule, StartAssignment assignment) {
    super.validate(validator, schedule, assignment);
    Program program = schedule.getProgram();
    Room room = assignment.getRoom();
    Section course = assignment.getSection();
    Set<Resource> courseRequirements = program.resourceRequirements(course);

    Set<Resource> bindingResources = program.bindingResources(room);
    validator.validateLocal(
        courseRequirements.containsAll(bindingResources),
        assignment,
        "Room contains all binding resources");
  }

  @Override
  public
      void
      validate(ScheduleValidator validator, Schedule schedule, PresentAssignment assignment) {
    super.validate(validator, schedule, assignment);
    Program program = schedule.getProgram();
    Room room = assignment.getRoom();
    Section course = assignment.getSection();
    ClassPeriod period = assignment.getPeriod();
    Set<Resource> courseRequirements = Sets.newHashSet(program.resourceRequirements(course));
    courseRequirements.removeAll(program.roomResources(room));

    ListMultimap<Resource, PresentAssignment> resourceDemands = ArrayListMultimap.create();
    for (PresentAssignment concurrent : schedule.occurringAt(period).values()) {
      for (Resource resource : program.resourceRequirements(concurrent.getSection())) {
        if (courseRequirements.contains(resource)) {
          resourceDemands.put(resource, concurrent);
        }
      }
    }

    for (Entry<Resource, Collection<PresentAssignment>> resourceEntry : resourceDemands
        .asMap()
        .entrySet()) {
      Resource resource = resourceEntry.getKey();
      List<PresentAssignment> concurrent = (List<PresentAssignment>) resourceEntry.getValue();
      Collections.shuffle(concurrent, rand);
      if (concurrent.size() >= resource.getFloatingCount()) {
        validator.validateGlobal(
            assignment,
            concurrent.subList(0, concurrent.size() + 1 - resource.getFloatingCount()),
            "Not enough of " + resource + " to go around");
      }
    }
  }
}
