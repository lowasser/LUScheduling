package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.NavigableMap;

import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Section;
import org.learningu.scheduling.logic.GlobalConflict;
import org.learningu.scheduling.logic.ScheduleLogic;
import org.learningu.scheduling.logic.ScheduleValidator;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * A mutable implementation of a {@code Schedule}.
 * 
 * @author lowasser
 */
public final class MutableSchedule extends Schedule {

  @Singleton
  public static final class Factory {
    private final Provider<ScheduleValidator> validatorProvider;

    private final ScheduleLogic logic;

    @Inject
    Factory(Provider<ScheduleValidator> validatorProvider, ScheduleLogic logic) {
      this.validatorProvider = validatorProvider;
      this.logic = logic;
    }

    public MutableSchedule create(Program program) {
      ImmutableMap.Builder<Room, NavigableMap<ClassPeriod, Section>> roomMapBuilder = ImmutableMap
          .builder();
      for (Room room : program.getRooms()) {
        roomMapBuilder.put(room, Maps.<ClassPeriod, Section> newTreeMap());
      }
      Map<Room, NavigableMap<ClassPeriod, Section>> roomMap = roomMapBuilder.build();
      return new MutableSchedule(program, roomMap, logic, validatorProvider);
    }
  }

  private final ScheduleLogic logic;

  private final Provider<ScheduleValidator> validatorProvider;

  private MutableSchedule(Program program,
      Map<Room, NavigableMap<ClassPeriod, Section>> startingTimeTable, ScheduleLogic logic,
      Provider<ScheduleValidator> validatorProvider) {
    super(program, startingTimeTable);
    this.logic = checkNotNull(logic);
    this.validatorProvider = checkNotNull(validatorProvider);
  }

  public ScheduleValidator putAssignment(StartAssignment assign) {
    ScheduleValidator validator = validatorProvider.get();
    logic.validate(validator, this, assign);
    if (validator.isValid()) {
      startingTimeTable.get(assign.getRoom()).put(assign.getPeriod(), assign.getSection());
    }
    return validator;
  }

  public Optional<StartAssignment> removeStartingAt(StartAssignment assign) {
    Optional<StartAssignment> startingAt = this.startingAt(assign.getRoom(), assign.getPeriod());
    if (startingAt.isPresent() && startingAt.get().equals(assign)) {
      startingTimeTable.get(assign.getRoom()).remove(assign.getPeriod());
      return startingAt;
    } else {
      return Optional.absent();
    }
  }

  public Optional<StartAssignment> removeOccurringAt(PresentAssignment assign) {
    return removeStartingAt(assign.getStartAssignment());
  }

  public Optional<StartAssignment> removeOccurringAt(ClassPeriod period, Room room) {
    Optional<PresentAssignment> occurring = occurringAt(period, room);
    if (occurring.isPresent()) {
      return removeOccurringAt(occurring.get());
    } else {
      return Optional.absent();
    }
  }

  /**
   * Removes all conflicting assignments. Returns {@code true} if there were no local conflicts and
   * the assignment was successfully added.
   */
  public boolean forcePutAssignment(StartAssignment assign) {
    ScheduleValidator validator = validatorProvider.get();

    logic.validate(validator, this, assign);
    for (PresentAssignment pAssign : assign.getPresentAssignments()) {
      logic.validate(validator, this, pAssign);
    }

    if (!validator.getLocalPresentConflicts().isEmpty()
        || !validator.getLocalStartConflicts().isEmpty()) {
      return false;
    }
    for (GlobalConflict<StartAssignment> conflict : validator.getGlobalStartConflicts()) {
      for (StartAssignment conflicting : conflict.getConflictingAssignments()) {
        removeStartingAt(conflicting);
      }
    }
    for (GlobalConflict<PresentAssignment> conflict : validator.getGlobalPresentConflicts()) {
      for (PresentAssignment conflicting : conflict.getConflictingAssignments()) {
        removeOccurringAt(conflicting);
      }
    }
    validator = putAssignment(assign);
    assert validator.isValid();
    return true;
  }
}
