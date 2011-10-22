package org.learningu.scheduling;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;
import org.learningu.scheduling.util.Condition;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultScheduleLogic.class)
public interface ScheduleLogic {

  Condition isValid(Schedule schedule);
}
