package org.edx.indonesiax.model.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lindaliu on 7/13/15.
 */

public enum AccessError {
    @SerializedName("course_not_started")
    START_DATE_ERROR,
    @SerializedName("not_visible_to_user")
    VISIBILITY_ERROR,
    @SerializedName("unfulfilled_milestones")
    MILESTONE_ERROR
}
