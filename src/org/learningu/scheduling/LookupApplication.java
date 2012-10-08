package org.learningu.scheduling;

import com.google.inject.Injector;
import com.google.inject.Module;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.learningu.scheduling.gui.LookupFrame;

import edu.uchicago.lowasser.flaginjection.Flags;

public class LookupApplication {
  public static void main(String[] args) throws IOException, InterruptedException,
      ExecutionException {
    Logger logger = Logger.getLogger("Autoscheduling");
    logger.fine("Initializing injector with flags");
    Injector injector = Flags.bootstrapFlagInjector(args, new AutoschedulingBaseModule());
    logger.fine("Injecting data source provider");
    AutoschedulerDataSource dataSource = injector.getInstance(AutoschedulerDataSource.class);
    logger.fine("Reading input files");
    Module dataModule = dataSource.buildModule();
    logger.fine("Bootstrapping into completely initialized injector");
    Injector dataInjector =
        injector.createChildInjector(dataModule, new AutoschedulingConfigModule());
    logger.fine("Initializing GUI");
    LookupFrame frame = dataInjector.getInstance(LookupFrame.class);
    frame.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    frame.setVisible(true);
  }
}
