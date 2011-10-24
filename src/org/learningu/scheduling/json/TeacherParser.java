package org.learningu.scheduling.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.learningu.scheduling.graph.Serial.SerialTeacher;

final class TeacherParser extends JsonParser<SerialTeacher> {

  @Override
  public SerialTeacher parseJsonToProto(JSONObject object) throws JSONException {
    SerialTeacher.Builder builder = SerialTeacher.newBuilder();
    builder.setTeacherId(object.getInt("uid"));
    builder.setName(object.getString("text"));
    JSONArray availableArray = object.getJSONArray("availability");
    for (int i = 0; i < availableArray.length(); i++) {
      builder.addAvailablePeriods(availableArray.getInt(i));
    }
    return builder.build();
  }
}
