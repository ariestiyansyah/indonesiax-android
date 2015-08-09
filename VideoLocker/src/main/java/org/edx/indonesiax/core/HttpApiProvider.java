package org.edx.indonesiax.core;

import android.app.Application;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.edx.indonesiax.base.MainApplication;
import org.edx.indonesiax.http.Api;
import org.edx.indonesiax.http.IApi;
import org.edx.indonesiax.http.RestApiManager;
import org.edx.indonesiax.util.Config;

/**
 * Created by hanning on 6/22/15.
 */
public class HttpApiProvider implements Provider<IApi> {

    @Inject
    Application application;
    @Inject
    Config config;

    @Override
    public IApi get() {
        if (MainApplication.RETROFIT_ENABLED ){
            return new RestApiManager(application);
        } else {
            return new Api(application);
        }
    }
}
