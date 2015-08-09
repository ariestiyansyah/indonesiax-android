package org.edx.indonesiax.task;

import android.content.Context;

import org.edx.indonesiax.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.indonesiax.services.ServiceManager;

public abstract class SyncLastAccessedTask extends Task<SyncLastAccessedSubsectionResponse> {

    String courseId ;
    String lastVisitedModuleId ;
    public SyncLastAccessedTask(Context context, String courseId, String lastVisitedModuleId) {
        super(context);
        this.courseId = courseId;
        this.lastVisitedModuleId = lastVisitedModuleId;
    }

    @Override
    public SyncLastAccessedSubsectionResponse call( ) throws Exception{
        try {

            if(courseId!=null && lastVisitedModuleId !=null){
                ServiceManager api = environment.getServiceManager();
                SyncLastAccessedSubsectionResponse res = api.syncLastAccessedSubsection(
                        courseId, lastVisitedModuleId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
