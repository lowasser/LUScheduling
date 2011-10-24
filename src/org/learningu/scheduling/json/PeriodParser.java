package org.learningu.scheduling.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.learningu.scheduling.graph.Serial.SerialDateTime;
import org.learningu.scheduling.graph.Serial.SerialPeriod;

final class PeriodParser extends JsonParser<SerialPeriod> {

  @Override
  public SerialPeriod parseJsonToProto(JSONObject object) throws JSONException {
    SerialPeriod.Builder builder = SerialPeriod.newBuilder();
    builder.setPeriodId(object.getInt("id"));
    if (object.has("start")) {
      builder.setStart(parseJsonDateTime(object.getJSONArray("start")));
    }
    if (object.has("end")) {
      builder.setEnd(parseJsonDateTime(object.getJSONArray("end")));
    }
    builder.setShortDescription(object.getString("short_description"));
    builder.setDescription(object.getString("description"));
    return builder.build();
  }

  private SerialDateTime parseJsonDateTime(JSONArray array) throws JSONException {
    SerialDateTime.Builder builder = SerialDateTime.newBuilder();
    builder.setYear(array.getInt(0));
    builder.setMonth(array.getInt(1));
    builder.setDay(array.getInt(2));
    builder.setHour(array.getInt(3));
    builder.setMinute(array.getInt(4));
    builder.setSecond(array.getInt(5));
    return builder.build();
  }

}
