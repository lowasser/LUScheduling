package org.learningu.scheduling.modules;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.graph.Subject;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.optimization.Scorer;
import org.learningu.scheduling.schedule.PresentAssignment;
import org.learningu.scheduling.schedule.Schedule;
import org.learningu.scheduling.schedule.StartAssignment;
import org.learningu.scheduling.scorers.SerialScorers.CompleteScorer;
import org.learningu.scheduling.scorers.SerialScorers.ScaledScorer;
import org.learningu.scheduling.scorers.SerialScorers.SerialScorerImpl;

public final class ScorerModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  enum ScorerImpl {
    TEACHERS_WITH_CLASSES_SCHEDULED {
      @Override
      void score(Schedule schedule, ScoreAccumulator accum) {
        Program program = schedule.getProgram();
        Set<Teacher> teachers = Sets.newHashSetWithExpectedSize(program.getTeachers().size());
        for (Section s : schedule.getScheduledSections()) {
          teachers.addAll(program.teachersFor(s));
        }
        accum.add(teachers.size());
      }
    },
    DISTINCT_COURSES_SCHEDULED {
      @Override
      void score(Schedule schedule, ScoreAccumulator accum) {
        Program program = schedule.getProgram();
        Set<Course> courses = Sets.newHashSetWithExpectedSize(program.getCourses().size());
        for (Section s : schedule.getScheduledSections()) {
          courses.add(s.getCourse());
        }
        accum.add(courses.size());
      }
    },
    SECTIONS_SCHEDULED {

      @Override
      void score(Schedule schedule, ScoreAccumulator accum) {
        accum.add(schedule.getScheduledSections().size());
      }
    },
    CLASS_HOURS_SCHEDULED {
      @Override
      void score(Schedule schedule, ScoreAccumulator accum) {
        for (Section s : schedule.getScheduledSections()) {
          accum.add(s.getPeriodLength());
        }
      }
    },
    STUDENT_CLASS_HOURS_SCHEDULED {
      @Override
      void score(Schedule schedule, ScoreAccumulator accum) {
        for (Section s : schedule.getScheduledSections()) {
          accum.add(s.getPeriodLength() * s.getEstimatedClassSize());
        }
      }
    },
    BACK_TO_BACK_CLASSES {
      @Override
      void score(Schedule schedule, ScoreAccumulator accum) {
        Program program = schedule.getProgram();
        ImmutableMap.Builder<Teacher, NavigableMap<ClassPeriod, StartAssignment>> assignmentBuilder = ImmutableMap
            .builder();
        for (Teacher teacher : program.getTeachers()) {
          assignmentBuilder.put(teacher, Maps.<ClassPeriod, StartAssignment> newTreeMap());
        }
        ImmutableMap<Teacher, NavigableMap<ClassPeriod, StartAssignment>> assignments = assignmentBuilder
            .build();
        for (StartAssignment assign : schedule.getStartAssignments()) {
          for (Teacher teacher : program.teachersFor(assign.getSection())) {
            assignments.get(teacher).put(assign.getPeriod(), assign);
          }
        }
        for (NavigableMap<ClassPeriod, StartAssignment> teacherSchedule : assignments.values()) {
          PeekingIterator<StartAssignment> assignmentIterator = Iterators
              .peekingIterator(teacherSchedule.values().iterator());
          while (assignmentIterator.hasNext()) {
            StartAssignment prev = assignmentIterator.next();
            if (!assignmentIterator.hasNext()) {
              break;
            }
            StartAssignment next = assignmentIterator.peek();
            if (backToBack(prev, next) && !prev.getRoom().equals(next.getRoom())) {
              accum.subtract(1.0);
            }
          }
        }
      }

      boolean backToBack(StartAssignment assign1, StartAssignment assign2) {
        ClassPeriod start1 = assign1.getPeriod();
        int length1 = assign1.getCourse().getPeriodLength();
        ClassPeriod start2 = assign2.getPeriod();
        return start2.getTimeBlock().equals(start1.getTimeBlock())
            && start2.getIndex() == start1.getIndex() + length1;
      }
    },
    GLOBAL_ATTENDANCE_LEVELS {
      @Override
      void score(Schedule schedule, ScoreAccumulator accum) {
        Program program = schedule.getProgram();
        Multiset<ClassPeriod> attendanceLevels = HashMultiset.create(program.getPeriods().size());
        for (PresentAssignment assign : schedule.getPresentAssignments()) {
          attendanceLevels.add(assign.getPeriod(), assign.getSection().getMaxClassSize());
        }
        int totalAttendance = attendanceLevels.size();
        for (ClassPeriod period : program.getPeriods()) {
          int actualAttendance = attendanceLevels.count(period);
          double expectedAttendance = program.getAttendanceRatio(period) * totalAttendance;
          if (expectedAttendance != 0.0) {
            double ratio = actualAttendance / expectedAttendance;
            accum.subtract(Math.abs(ratio - 1.0));
          }
        }
      }
    },
    SUBJECT_ATTENDANCE_LEVELS {
      @Override
      void score(Schedule schedule, ScoreAccumulator accum) {
        Program program = schedule.getProgram();
        Map<Subject, Multiset<ClassPeriod>> attendanceLevels = Maps.newHashMap();
        for (Subject subj : program.getSubjects()) {
          attendanceLevels.put(subj, HashMultiset.<ClassPeriod> create());
        }

        for (PresentAssignment assign : schedule.getPresentAssignments()) {
          attendanceLevels.get(assign.getSection().getSubject()).add(
              assign.getPeriod(),
              assign.getSection().getMaxClassSize());
        }
        for (Map.Entry<Subject, Multiset<ClassPeriod>> subjectEntry : attendanceLevels.entrySet()) {
          int totalAttendance = subjectEntry.getValue().size();
          for (ClassPeriod period : program.getPeriods()) {
            int actualAttendance = subjectEntry.getValue().count(period);
            double expectedAttendance = program.getAttendanceRatio(period) * totalAttendance;
            if (expectedAttendance != 0.0) {
              double ratio = actualAttendance / expectedAttendance;
              accum.subtract(Math.abs(ratio - 1.0));
            }
          }
        }
      }
    },
    UNUSED_ROOMS {
      @Override
      void score(Schedule schedule, ScoreAccumulator accum) {
        Program program = schedule.getProgram();
        for (Room room : program.getRooms()) {
          if (schedule.startingIn(room).isEmpty()) {
            accum.add(1);
          }
        }
      }
    };
    abstract void score(Schedule schedule, ScoreAccumulator accum);
  }

  static final class ScoreAccumulator {
    private final double exponent;
    private final double scale;
    private double accum;

    ScoreAccumulator(double exponent, double scale) {
      this.exponent = exponent;
      this.scale = scale;
      this.accum = 0.0;
    }

    public double getTotal() {
      return scale * accum;
    }

    public void add(double score) {
      accum += Math.pow(score, exponent);
    }

    public void subtract(double score) {
      accum -= Math.pow(score, exponent);
    }
  }

  @Provides
  @Singleton
  Scorer<Schedule> deserialize(CompleteScorer serial) {
    ImmutableList.Builder<Scorer<Schedule>> componentsBuilder = ImmutableList.builder();
    for (ScaledScorer scaled : serial.getComponentList()) {
      componentsBuilder.add(deserialize(scaled));
    }
    final ImmutableList<Scorer<Schedule>> components = componentsBuilder.build();
    return new Scorer<Schedule>() {
      @Override
      public double score(Schedule input) {
        double total = 0;
        for (Scorer<Schedule> scorer : components) {
          total += scorer.score(input);
        }
        return total;
      }
    };
  }

  private Scorer<Schedule> deserialize(ScaledScorer scorer) {
    final double multiplier = scorer.getMultiplier();
    final double exponent = scorer.getExponent();
    final ScorerImpl impl = deserialize(scorer.getImpl());
    return new Scorer<Schedule>() {

      @Override
      public double score(Schedule input) {
        ScoreAccumulator accum = new ScoreAccumulator(exponent, multiplier);
        impl.score(input, accum);
        return accum.getTotal();
      }
    };
  }

  private ScorerImpl deserialize(SerialScorerImpl impl) {
    switch (impl) {
      case CLASS_HOURS_SCHEDULED:
        return ScorerImpl.CLASS_HOURS_SCHEDULED;
      case DISTINCT_CLASSES_SCHEDULED:
        return ScorerImpl.DISTINCT_COURSES_SCHEDULED;
      case SECTIONS_SCHEDULED:
        return ScorerImpl.SECTIONS_SCHEDULED;
      case BACK_TO_BACK_CLASSES:
        return ScorerImpl.BACK_TO_BACK_CLASSES;
      case STUDENT_CLASS_HOURS_SCHEDULED:
        return ScorerImpl.STUDENT_CLASS_HOURS_SCHEDULED;
      case TEACHERS_WITH_CLASSES_SCHEDULED:
        return ScorerImpl.TEACHERS_WITH_CLASSES_SCHEDULED;
      case GLOBAL_ATTENDANCE_LEVELS:
        return ScorerImpl.GLOBAL_ATTENDANCE_LEVELS;
      case SUBJECT_ATTENDANCE_LEVELS:
        return ScorerImpl.SUBJECT_ATTENDANCE_LEVELS;
      case UNUSED_ROOMS:
        return ScorerImpl.UNUSED_ROOMS;
      default:
        throw new AssertionError();
    }
  }

}
