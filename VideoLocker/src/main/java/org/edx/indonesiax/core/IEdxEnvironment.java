package org.edx.indonesiax.core;


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

/**
 * TODO - we should decompose this class into environment setting and service provider settings.
 */
public interface IEdxEnvironment {

    IDatabase getDatabase();

    IStorage getStorage();

    IDownloadManager getDownloadManager();

    ImageCacheManager getImageCacheManager();

    UserPrefs getUserPrefs();

    ISegment getSegment();

    NotificationDelegate getNotificationDelegate();

    Router getRouter();

    Config getConfig();

    ServiceManager getServiceManager();
}
