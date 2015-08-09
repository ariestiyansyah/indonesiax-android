package org.edx.indonesiax.view.common;

import org.edx.indonesiax.model.course.CourseComponent;

/**
 * Created by hanning on 6/9/15.
 */
public interface RunnableCourseComponent extends Runnable{
    CourseComponent getCourseComponent();
}
