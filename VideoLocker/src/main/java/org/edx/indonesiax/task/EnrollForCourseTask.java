package org.edx.indonesiax.task;

import android.content.Context;

import org.edx.indonesiax.services.ServiceManager;

public abstract class EnrollForCourseTask extends Task<Boolean> {

    String courseId;
    boolean emailOptIn ;
    public EnrollForCourseTask(Context context, String courseId, boolean emailOptIn) {
        super(context);
        this.courseId = courseId;
        this.emailOptIn = emailOptIn;
    }

    @Override
    public Boolean call( ) throws Exception{
        try {

            if(courseId!=null){
                ServiceManager api = environment.getServiceManager();
                try {
                    return api.enrollInACourse(courseId,emailOptIn);
                } catch(Exception ex) {
                    logger.error(ex, true);
                }
            }
            return false;
        } catch(Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return false;
    }
}
