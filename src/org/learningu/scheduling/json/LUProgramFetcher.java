package org.learningu.scheduling.json;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.learningu.scheduling.flags.FlagsModule;
import org.learningu.scheduling.flags.OptionsModule;

public final class LUProgramFetcher {
  public static void main(final String[] args) throws ClientProtocolException, IOException {
    Injector flaggedInjector = OptionsModule.buildOptionsInjector(
        args,
        FlagsModule.create(LUAuthenticator.class),
        FlagsModule.create(LUProgramRetriever.class),
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(HttpClient.class).to(DefaultHttpClient.class).in(Scopes.SINGLETON);
          }
        });
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
