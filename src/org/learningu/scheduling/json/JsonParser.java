package org.learningu.scheduling.json;

import org.json.JSONException;
import org.json.JSONObject;

abstract class JsonParser<T> {
  public abstract T parseJsonToProto(JSONObject object) throws JSONException;
}
