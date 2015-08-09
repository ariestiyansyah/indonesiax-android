package org.edx.indonesiax.core;

import android.app.Application;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.edx.indonesiax.module.analytics.ISegment;
import org.edx.indonesiax.module.analytics.ISegmentEmptyImpl;
import org.edx.indonesiax.module.analytics.ISegmentImpl;
import org.edx.indonesiax.util.Config;

/**
 * Created by hanning on 6/22/15.
 */
public class SegmentProvider implements Provider<ISegment> {

    @Inject
    Application application;
    @Inject
    Config config;

    @Override
    public ISegment get() {
        if (config.getSegmentConfig().isEnabled()) {
            return new ISegmentImpl( );
        }
        else {
           return new ISegmentEmptyImpl();
        }
    }
}
