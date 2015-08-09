package org.edx.indonesiax.core;


import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.indonesiax.module.analytics.ISegment;
import org.edx.indonesiax.module.db.IDatabase;
import org.edx.indonesiax.module.download.IDownloadManager;
import org.edx.indonesiax.module.notification.NotificationDelegate;
import org.edx.indonesiax.module.prefs.UserPrefs;
import org.edx.indonesiax.module.storage.IStorage;
import org.edx.indonesiax.services.ServiceManager;
import org.edx.indonesiax.util.Config;
import org.edx.indonesiax.util.images.ImageCacheManager;
import org.edx.indonesiax.view.Router;

@Singleton
public class EdxEnvironment implements IEdxEnvironment{

    @Inject
    Context context;

    @Inject
    IDatabase database;

    @Inject
    IStorage storage;

    @Inject
    IDownloadManager downloadManager;

    @Inject
    UserPrefs userPrefs;

    @Inject
    ISegment segment;

    @Inject
    NotificationDelegate notificationDelegate;

    @Inject
    Router router;

    @Inject
    Config config;

    @Inject
    ImageCacheManager imageCacheManager;

    @Inject
    ServiceManager serviceManager;

    @Override
    public IDatabase getDatabase() {
        return database;
    }

    @Override
    public IDownloadManager getDownloadManager() {
        return downloadManager;
    }

    @Override
    public UserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public ISegment getSegment() {
        return segment;
    }

    @Override
    public NotificationDelegate getNotificationDelegate() {
        return notificationDelegate;
    }

    @Override
    public Router getRouter() {
        return router;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public IStorage getStorage() {
        return storage;
    }

    @Override
    public ImageCacheManager getImageCacheManager() {
        return imageCacheManager;
    }

    @Override
    public ServiceManager getServiceManager() {
        return serviceManager;
    }
}
