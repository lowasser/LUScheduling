package org.learningu.scheduling.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.learningu.scheduling.FlagOptionsModule;
import org.learningu.scheduling.FlagsModule;
import org.learningu.scheduling.OptionsModule;
import org.learningu.scheduling.annotations.RuntimeArguments;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;

public final class LUProgramFetcher {
  public static void main(final String[] args) throws ClientProtocolException, IOException {
    Injector baseInjector = Guice.createInjector(
        new FlagOptionsModule(),
        FlagsModule.create(LUAuthenticator.class),
        FlagsModule.create(LUProgramRetriever.class),
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(HttpClient.class).to(DefaultHttpClient.class).in(Scopes.SINGLETON);
          }

          @SuppressWarnings("unused")
          @Provides
          @RuntimeArguments
          List<String> runtimeArgs() {
            return Arrays.asList(args);
          }
        });
    Injector flaggedInjector = baseInjector.createChildInjector(baseInjector
        .getInstance(OptionsModule.class));
    try {
      LUAuthenticator auth = flaggedInjector.getInstance(LUAuthenticator.class);
      auth.authenticate();
      LUProgramRetriever retriever = flaggedInjector.getInstance(LUProgramRetriever.class);
      retriever.getJson("ajax_teachers");
    } finally {
      flaggedInjector.getInstance(HttpClient.class).getConnectionManager().shutdown();
    }
  }
}
