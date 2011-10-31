package org.learningu.scheduling.pretty;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;

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
      ImmutableSortedMap<ClassPeriod, StartAssignment> periodMap = ImmutableSortedMap
          .copyOf(schedule.startingIn(room));
      for (Map.Entry<ClassPeriod, StartAssignment> entry : periodMap.entrySet()) {
        for (PresentAssignment present : entry.getValue().getPresentAssignments()) {
          Csv.RowBuilder rowBuilder = Csv.newRowBuilder();
          rowBuilder
              .addBlank()
              .add(present.getPeriod().getDescription())
              .add(present.getSection().getTitle());
          builder.add(rowBuilder.build());
        }
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
      SortedMap<ClassPeriod, StartAssignment> periods = Maps.newTreeMap();
      for (Section s : sections) {
        StartAssignment assign = assignments.get(s);
        if (assign != null) {
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
    }

    return builder.build();
  }
}
