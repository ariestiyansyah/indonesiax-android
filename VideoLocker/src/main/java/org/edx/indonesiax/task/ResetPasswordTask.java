package org.edx.indonesiax.task;

import android.content.Context;

import org.edx.indonesiax.model.api.ResetPasswordResponse;
import org.edx.indonesiax.services.ServiceManager;

public abstract class ResetPasswordTask extends Task<ResetPasswordResponse> {

    String emailId;
    public ResetPasswordTask(Context context,String emailId) {
        super(context);
        this.emailId = emailId;
    }

    @Override
    public ResetPasswordResponse call() throws Exception{
        try {

            if(emailId!=null){
                ServiceManager api = environment.getServiceManager();
                ResetPasswordResponse res = api.resetPassword(emailId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
