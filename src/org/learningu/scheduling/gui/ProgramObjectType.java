package org.learningu.scheduling.gui;

import com.google.common.collect.Lists;

import java.util.List;

import org.learningu.scheduling.graph.Building;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.ProgramObject;
import org.learningu.scheduling.graph.Resource;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Teacher;

enum ProgramObjectType {
  ROOM {
    @Override
    List<? extends ProgramObject<?>> retrieveMatching(Program program, String query) {
      List<Room> rooms = Lists.newArrayList();
      query = query.toLowerCase();
      for (Room r : program.getRooms()) {
        if (r.getName().toLowerCase().contains(query)) {
          rooms.add(r);
          if (rooms.size() >= LookupFrame.RETURN_CAP) {
            break;
          }
        }
      }
      return rooms;
    }
  },
  SECTION {
    @Override
    List<? extends ProgramObject<?>> retrieveMatching(Program program, String query) {
      List<Section> sections = Lists.newArrayList();
      query = query.toLowerCase();
      for (Section s : program.getSections()) {
        if (matches(s, query)) {
          sections.add(s);
          if (sections.size() >= LookupFrame.RETURN_CAP) {
            break;
          }
        }
      }
      return sections;
    }

    private boolean matches(Section s, String query) {
      if (s.getTitle().toLowerCase().contains(query)) {
        return true;
      }
      for (Teacher t : s.getCourse().getTeachers()) {
        if (t.getName().toLowerCase().contains(query)) {
          return true;
        }
      }
      return false;
    }
  },
  TEACHER {
    @Override
    List<? extends ProgramObject<?>> retrieveMatching(Program program, String query) {
      List<Teacher> teachers = Lists.newArrayList();
      query = query.toLowerCase();
      for (Teacher t : program.getTeachers()) {
        if (t.getName().toLowerCase().contains(query)) {
          teachers.add(t);
          if (teachers.size() >= LookupFrame.RETURN_CAP) {
            break;
          }
        }
      }
      return teachers;
    }
  },
  RESOURCE {
    @Override
    List<? extends ProgramObject<?>> retrieveMatching(Program program, String query) {
      List<Resource> resources = Lists.newArrayList();
      query = query.toLowerCase();
      for (Resource r : program.getResources()) {
        if (r.getDescription().toLowerCase().contains(query)) {
          resources.add(r);
          if (resources.size() >= LookupFrame.RETURN_CAP) {
            break;
          }
        }
      }
      return resources;
    }
  },
  BUILDING {
    @Override
    List<? extends ProgramObject<?>> retrieveMatching(Program program, String query) {
      List<Building> buildings = Lists.newArrayList();
      query = query.toLowerCase();
      for (Building b : program.getBuildings()) {
        if (b.getName().toLowerCase().contains(query)) {
          buildings.add(b);
          if (buildings.size() >= LookupFrame.RETURN_CAP) {
            break;
          }
        }
      }
      return buildings;
    }
  };

  abstract List<? extends ProgramObject<?>> retrieveMatching(Program program, String query);
}