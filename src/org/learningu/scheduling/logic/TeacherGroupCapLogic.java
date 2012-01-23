package org.learningu.scheduling.logic;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TeacherGroup;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;

public final class TeacherGroupCapLogic extends ScheduleLogic {
  private final Random random;

  @Inject
  TeacherGroupCapLogic(Random random) {
    this.random = random;
  }

  @Override
  public void validate(ScheduleValidator validator, Schedule schedule,
      PresentAssignment newAssignment) {
    Program program = schedule.getProgram();

    ListMultimap<TeacherGroup, PresentAssignment> groups = ArrayListMultimap.create();
    for (Teacher t : program.teachersFor(newAssignment.getSection())) {
      for (TeacherGroup g : program.getGroups(t)) {
        groups.put(g, newAssignment);
      }
    }
    Predicate<TeacherGroup> relevant = Predicates.in(ImmutableSet.copyOf(groups.keySet()));

    for (PresentAssignment assign : schedule.occurringAt(newAssignment.getPeriod()).values()) {
      for (Teacher t : program.teachersFor(assign.getSection())) {
        for (TeacherGroup g : Iterables.filter(program.getGroups(t), relevant)) {
          groups.put(g, assign);
        }
      }
    }

    for (Map.Entry<TeacherGroup, Collection<PresentAssignment>> entry : groups.asMap().entrySet()) {
      TeacherGroup g = entry.getKey();
      Collection<PresentAssignment> assigns = entry.getValue();

      if (assigns.size() > g.getCap()) {
        Multiset<PresentAssignment> conflicting = HashMultiset.create(assigns);
        conflicting.elementSet().remove(newAssignment);
        List<PresentAssignment> distinctConflicts = Lists.newArrayList(conflicting.elementSet());
        List<PresentAssignment> deleted = Lists.newArrayList();
        // Pick elements at random to conflict with, until we're back under the cap.
        int newSize = assigns.size();
        while (!distinctConflicts.isEmpty() && newSize > g.getCap()) {
          int index = random.nextInt(distinctConflicts.size());
          PresentAssignment toDelete = distinctConflicts.remove(index);
          deleted.add(toDelete);
          newSize -= conflicting.setCount(toDelete, 0);
        }
        validator.validateGlobal(
            newAssignment,
            deleted,
            "Too many members of group " + g.getName() + " teaching at once");
      }
    }
  }
}
