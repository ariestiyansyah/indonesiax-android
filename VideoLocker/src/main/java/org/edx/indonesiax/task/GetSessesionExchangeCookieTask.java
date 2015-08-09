package org.edx.indonesiax.task;

import android.content.Context;

import org.edx.indonesiax.services.ServiceManager;

import java.net.HttpCookie;
import java.util.List;

public abstract class GetSessesionExchangeCookieTask extends Task<List<HttpCookie>> {

    public GetSessesionExchangeCookieTask(Context context) {
        super(context);
    }

    @Override
    public List<HttpCookie> call( ) throws Exception{
        try {
                ServiceManager api = environment.getServiceManager();
                return api.getSessionExchangeCookie();
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }

}
