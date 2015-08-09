package org.edx.indonesiax.core;

import android.app.Application;
import android.graphics.Bitmap;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.edx.indonesiax.util.images.ImageCacheManager;
import org.edx.indonesiax.util.images.RequestManager;

/**
 * Created by hanning on 6/22/15.
 */
public class ImageCacheProvider implements Provider<ImageCacheManager> {

    @Inject
    Application application;

    @Override
    public ImageCacheManager get() {
        int DISK_IMAGECACHE_SIZE = 1024*1024*10;
        Bitmap.CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
        //PNG is lossless so quality is ignored but must be provided
        int DISK_IMAGECACHE_QUALITY = 100;

        RequestManager.init(application);
        return new ImageCacheManager(application,
            application.getPackageCodePath()
            , DISK_IMAGECACHE_SIZE
            , DISK_IMAGECACHE_COMPRESS_FORMAT
            , DISK_IMAGECACHE_QUALITY
            , ImageCacheManager.CacheType.MEMORY);
    }
}
