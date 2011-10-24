package org.learningu.scheduling.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.learningu.scheduling.graph.Serial.SerialRoom;

final class RoomParser extends JsonParser<SerialRoom> {
  private int uid = 0;

  @Override
  public SerialRoom parseJsonToProto(JSONObject jsonRoom) throws JSONException {
    SerialRoom.Builder roomBuilder = SerialRoom.newBuilder();
    roomBuilder.setRoomId(uid++);
    roomBuilder.setCapacity(jsonRoom.getInt("num_students"));
    roomBuilder.setName(jsonRoom.getString("uid"));
    JSONArray jsonPeriods = jsonRoom.getJSONArray("availability");
    for (int j = 0; j < jsonPeriods.length(); j++) {
      roomBuilder.addAvailablePeriods(jsonPeriods.getInt(j));
    }
    return roomBuilder.build();
  }
}
