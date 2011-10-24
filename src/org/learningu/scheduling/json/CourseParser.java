package org.learningu.scheduling.json;

import java.math.RoundingMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.learningu.scheduling.graph.Serial.SerialCourse;

import com.google.common.math.DoubleMath;

final class CourseParser extends JsonParser<SerialCourse> {

  @Override
  public SerialCourse parseJsonToProto(JSONObject object) throws JSONException {
    SerialCourse.Builder builder = SerialCourse.newBuilder();
    if (!object.isNull("optimal_class_size")) {
      builder.setEstimatedClassSize(object.getInt("optimal_class_size"));
    }
    builder.setCourseId(object.getInt("id"));
    builder.setCourseTitle(object.getString("text"));
    builder.setPeriodLength(DoubleMath.roundToInt(
        object.getDouble("length"),
        RoundingMode.HALF_EVEN));
    JSONArray teachersArray = object.getJSONArray("teachers");
    for (int i = 0; i < teachersArray.length(); i++) {
      builder.addTeacherIds(teachersArray.getInt(i));
    }
    JSONArray resourcesArray = object.getJSONArray("resource_requests");
    for (int i = 0; i < resourcesArray.length(); i++) {
      builder.addRequiredProperties(resourcesArray.getInt(i));
    }
    return builder.build();
  }

}
