package org.learningu.scheduling.json;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.learningu.scheduling.annotations.Flag;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public final class LUProgramRetriever {
  private final String hostname;
  private final HttpClient client;
  private final Gson gson;

  @Flag("program")
  private final String program;

  @Inject
  LUProgramRetriever(
      @Named("hostname") String hostname,
      @Named("program") String program,
      HttpClient client,
      Gson gson) {
    this.hostname = hostname;
    this.program = program;
    this.client = client;
    this.gson = gson;
  }

  private URI queryUri(String query) {
    try {
      return new URI(String.format("http://%s/manage/%s/%s", hostname, program, query));
    } catch (URISyntaxException e) {
      throw Throwables.propagate(e);
    }
  }

  public void getJson(String query) throws ClientProtocolException, IOException {
    HttpGet httpGet = new HttpGet(queryUri(query));
    String response = client.execute(httpGet, new ResponseHandler<String>() {

      @Override
      public String handleResponse(HttpResponse arg0) throws ClientProtocolException, IOException {
        return EntityUtils.toString(arg0.getEntity());
      }
    });
    System.out.println(response);
  }

  static final class JsonTeacher {
    final String text;
    final int uid;
    final int[] availability;

    JsonTeacher(String text, int uid, int[] availability) {
      this.text = text;
      this.uid = uid;
      this.availability = availability;
    }
  }
}
