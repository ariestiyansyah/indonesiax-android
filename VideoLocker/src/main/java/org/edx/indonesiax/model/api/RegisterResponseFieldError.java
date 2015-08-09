package org.edx.indonesiax.model.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rohan on 3/13/15.
 */
public class RegisterResponseFieldError {

    private @SerializedName("user_message") String userMessage;

    public String getUserMessage() {
        return userMessage;
    }
}
