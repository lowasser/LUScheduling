package org.learningu.scheduling.json;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.learningu.scheduling.annotations.Flag;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public final class LUAuthenticator {
  @Flag("username")
  private final String username;

  @Flag("password")
  private final String password;

  @Flag("hostname")
  private final String hostname;

  private final HttpClient client;

  private final Logger logger;

  @Inject
  LUAuthenticator(
      @Named("username") String username,
      @Named("password") String password,
      @Named("hostname") String hostname,
      @Named("program") String program,
      Logger logger,
      HttpClient client) {
    this.username = username;
    this.password = password;
    this.hostname = hostname;
    this.logger = logger;
    this.client = client;
  }

  private UrlEncodedFormEntity authEntity() {
    try {
      return new UrlEncodedFormEntity(ImmutableList.of(
          new BasicNameValuePair("username", username),
          new BasicNameValuePair("password", password)), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

  private HttpPost authPost() {
    HttpPost httppost = new HttpPost("http://" + hostname + "/myesp/ajax_login/");
    httppost.setEntity(authEntity());
    return httppost;
  }

  public void authenticate() throws ClientProtocolException, IOException {
    HttpPost post = authPost();
    logger.log(Level.FINE, "Making post: {0}", EntityUtils.toString(post.getEntity()));
    String response = client.execute(post, new ResponseHandler<String>() {
      @Override
      public String handleResponse(HttpResponse arg0) throws ClientProtocolException, IOException {
        return EntityUtils.toString(arg0.getEntity());
      }
    });
    logger.log(Level.FINE, "Received response {0}", response);
  }
}
