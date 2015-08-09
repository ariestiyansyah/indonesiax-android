package org.edx.indonesiax.core;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.edx.indonesiax.module.notification.DummyNotificationDelegate;
import org.edx.indonesiax.module.notification.NotificationDelegate;
import org.edx.indonesiax.module.notification.ParseNotificationDelegate;
import org.edx.indonesiax.util.Config;

/**
 * Created by hanning on 6/22/15.
 */
public class NotificationProvider  implements Provider<NotificationDelegate> {

    @Inject
    Config config;

    @Override
    public NotificationDelegate get() {
        if ( config.isNotificationEnabled() ) {
            Config.ParseNotificationConfig parseNotificationConfig =
                config.getParseNotificationConfig();
            if (parseNotificationConfig.isEnabled()) {
                return new ParseNotificationDelegate();
            }
            else {
                return new DummyNotificationDelegate();
            }
        }
        else {
            return new DummyNotificationDelegate();
        }
    }
}
