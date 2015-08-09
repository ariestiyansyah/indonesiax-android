package org.edx.indonesiax.model.api;

import java.io.Serializable;

public class ProfileModel implements Serializable {

    public Long id;
    public String username;
    public String email;
    public String name;
    public String course_enrollments;
    // public String url;

    // hold the json response as it is
    public String json;
}
