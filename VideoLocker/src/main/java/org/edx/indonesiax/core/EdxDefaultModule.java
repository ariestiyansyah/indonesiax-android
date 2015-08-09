package org.edx.indonesiax.core;

import android.content.Context;

import com.google.inject.AbstractModule;

import org.edx.indonesiax.base.MainApplication;
import org.edx.indonesiax.http.Api;
import org.edx.indonesiax.http.IApi;
import org.edx.indonesiax.http.RestApiManager;
import org.edx.indonesiax.module.analytics.ISegment;
import org.edx.indonesiax.module.analytics.ISegmentEmptyImpl;
import org.edx.indonesiax.module.analytics.ISegmentImpl;
import org.edx.indonesiax.module.analytics.ISegmentTracker;
import org.edx.indonesiax.module.analytics.ISegmentTrackerImpl;
import org.edx.indonesiax.module.db.IDatabase;
import org.edx.indonesiax.module.db.impl.IDatabaseImpl;
import org.edx.indonesiax.module.download.IDownloadManager;
import org.edx.indonesiax.module.download.IDownloadManagerImpl;
import org.edx.indonesiax.module.notification.DummyNotificationDelegate;
import org.edx.indonesiax.module.notification.NotificationDelegate;
import org.edx.indonesiax.module.notification.ParseNotificationDelegate;
import org.edx.indonesiax.module.storage.IStorage;
import org.edx.indonesiax.module.storage.Storage;
import org.edx.indonesiax.util.Config;

public class EdxDefaultModule extends AbstractModule {
    //if your module requires a context, add a constructor that will be passed a context.
    private Context context;

    //with RoboGuice 3.0, the constructor for AbstractModule will use an `Application`, not a `Context`
    public EdxDefaultModule(Context context) {
        this.context = context;
    }

    @Override
    public void configure() {
        Config config = new Config(context);

        bind(IDatabase.class).to(IDatabaseImpl.class);
        bind(IStorage.class).to(Storage.class);
        bind(ISegmentTracker.class).to(ISegmentTrackerImpl.class);
        if (config.getSegmentConfig().isEnabled()) {
            bind(ISegment.class).to(ISegmentImpl.class);
        }
        else {
            bind(ISegment.class).to(ISegmentEmptyImpl.class);
        }

        bind(IDownloadManager.class).to(IDownloadManagerImpl.class);

        if (MainApplication.RETROFIT_ENABLED ){
            bind(IApi.class).to(RestApiManager.class);
        } else {
            bind(IApi.class).to(Api.class);
        }

        if ( config.isNotificationEnabled() ) {
            Config.ParseNotificationConfig parseNotificationConfig =
                config.getParseNotificationConfig();
            if (parseNotificationConfig.isEnabled()) {
                bind(NotificationDelegate.class).to(ParseNotificationDelegate.class);
            }
            else {
                bind(NotificationDelegate.class).to(DummyNotificationDelegate.class);
            }
        }
        else {
            bind(NotificationDelegate.class).to(DummyNotificationDelegate.class);
        }

        bind(IEdxEnvironment.class).to(EdxEnvironment.class);

    }
}
