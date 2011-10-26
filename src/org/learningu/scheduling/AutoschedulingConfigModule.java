package org.learningu.scheduling;

import java.util.concurrent.ExecutorService;

import org.learningu.scheduling.logic.LogicProvider;
import org.learningu.scheduling.logic.ScheduleLogic;
import org.learningu.scheduling.pass.PassModule;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public final class AutoschedulingConfigModule extends AbstractModule {
  private final OptionsModule optionsModule;

  private final PassModule passModule;

  @Inject
  AutoschedulingConfigModule(OptionsModule optionsModule, PassModule passModule) {
    this.optionsModule = optionsModule;
    this.passModule = passModule;
  }

  @Override
  protected void configure() {
    install(optionsModule);
    install(passModule);
    bind(ScheduleLogic.class).toProvider(LogicProvider.class);
    bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class);
  }
}
