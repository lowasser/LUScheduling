package org.learningu.scheduling.pretty;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;

public class PrettySchedulePrinters {
  private PrettySchedulePrinters() {
  }

  public static Csv buildRoomScheduleCsv(Schedule schedule) {
    Csv.Builder builder = Csv.newBuilder();
    Program program = schedule.getProgram();
    for (Room room : program.getRooms()) {
      builder.add(Csv.newRowBuilder().add(room.getName()).build());
      for (ClassPeriod period : Ordering.natural().sortedCopy(program.compatiblePeriods(room))) {
        Optional<PresentAssignment> occurring = schedule.occurringAt(period, room);
        Csv.RowBuilder rowBuilder = Csv.newRowBuilder();
        rowBuilder.addBlank().add(period.getDescription());
        rowBuilder.add(occurring.isPresent() ? occurring.get().getSection().getTitle()
            : "UNASSIGNED");
        builder.add(rowBuilder.build());
      }
    }
    return builder.build();
  }

  public static Csv buildTeacherScheduleCsv(Schedule schedule) {
    Csv.Builder builder = Csv.newBuilder();
    Program program = schedule.getProgram();
    Map<Section, StartAssignment> assignments = schedule.getAssignmentsBySection();
    for (Teacher t : program.getTeachers()) {
      builder.add(Csv.newRowBuilder().add(t.getName()).build());
      Set<Section> sections = program.getCoursesForTeacher(t);
      Set<Section> unassigned = Sets.newHashSet(sections);
      SortedMap<ClassPeriod, StartAssignment> periods = Maps.newTreeMap();
      for (Section s : sections) {
        StartAssignment assign = assignments.get(s);
        if (assign != null) {
          unassigned.remove(assign.getSection());
          for (PresentAssignment pAssign : assign.getPresentAssignments()) {
            periods.put(pAssign.getPeriod(), assign);
          }
        }
      }

      for (Entry<ClassPeriod, StartAssignment> entry : periods.entrySet()) {
        Csv.RowBuilder rowBuilder = Csv.newRowBuilder();
        rowBuilder
            .addBlank()
            .add(entry.getKey().getDescription())
            .add(entry.getValue().getRoom().getName())
            .add(entry.getValue().getSection().getTitle());
        builder.add(rowBuilder.build());
      }
      for (Section s : unassigned) {
        Csv.RowBuilder rowBuilder = Csv.newRowBuilder();
        rowBuilder.addBlank().add("UNASSIGNED").addBlank().add(s.getTitle());
        builder.add(rowBuilder.build());
      }
    }

    return builder.build();
  }
}
