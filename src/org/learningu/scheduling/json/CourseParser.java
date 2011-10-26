package org.learningu.scheduling.json;

import java.math.RoundingMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.learningu.scheduling.graph.SerialGraph.SerialSection;

import com.google.common.math.DoubleMath;

final class CourseParser extends JsonParser<SerialSection> {

  @Override
  public SerialSection parseJsonToProto(JSONObject object) throws JSONException {
    SerialSection.Builder builder = SerialSection.newBuilder();
    if (!object.isNull("optimal_class_size")) {
      builder.setEstimatedClassSize(object.getInt("optimal_class_size"));
    }
    builder.setSectionId(object.getInt("id"));
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
      builder.addRequiredProperty(resourcesArray.getInt(i));
    }
    return builder.build();
  }

}
