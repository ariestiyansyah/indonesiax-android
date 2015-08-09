package org.edx.indonesiax.module.notification;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parse.ParseException;
import com.parse.ParseInstallation;

import org.edx.indonesiax.base.MainApplication;
import org.edx.indonesiax.logger.Logger;
import org.edx.indonesiax.module.prefs.PrefManager;
import org.edx.indonesiax.view.CourseDetailTabActivity;
import org.edx.indonesiax.view.MyCoursesListActivity;

import java.util.Locale;

/**
 * Common helper for Parse Notification
 */
public class ParseHandleHelper {
    public static final String COURSE_ANNOUNCEMENT_ACTION = "course.announcement";

    private static final Logger logger = new Logger(ParseHandleHelper.class.getName());

    public static CourseUpdateNotificationPayload extractPayload(android.content.Intent intent) {
        try {
            String payloadStr = intent.getExtras().getString("com.parse.Data");
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(payloadStr, CourseUpdateNotificationPayload.class);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Activity type is determined by action field of payload. By default, it will use
     * MyCoursesListActivity
     * @param payload
     * @return
     */
    public static java.lang.Class<? extends android.app.Activity> getActivityClass(BaseNotificationPayload payload){
        if ( payload == null ){
            return MyCoursesListActivity.class;
        }
        String action = payload.getAction();
        if ( COURSE_ANNOUNCEMENT_ACTION.equals(action) ) {
            return CourseDetailTabActivity.class;
        }
        return MyCoursesListActivity.class;
    }

    /**
     * check if user current language setting is different from that saved in parse.
     * if it is different, try to save language setting to parse.
     *
     */
    public static void tryToSaveLanguageSetting( ){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        final String languageKey = "preferredLanguage";
        final String countryKey = "preferredLanguage";
        String savedPreferredLanguage = installation.getString(languageKey);
        String savedPreferredCountry = installation.getString(countryKey);
        Locale locale = Locale.getDefault();
        String currentPreferredLanguage = locale.getLanguage();
        String currentPreferredCountry = locale.getCountry();
        boolean dirty = false;
        if (!currentPreferredLanguage.equals(savedPreferredLanguage) ) {
            installation.put(languageKey, currentPreferredLanguage);
            dirty = true;
        }
        if (!currentPreferredCountry.equals(savedPreferredCountry) ) {
            installation.put(countryKey, currentPreferredCountry);
            dirty = true;
        }
        if ( dirty ) {
            try {
                PrefManager.AppInfoPrefManager pmanager = new PrefManager.AppInfoPrefManager(MainApplication.instance());
                pmanager.setAppSettingNeedSyncWithParse(false);
                installation.saveInBackground(new com.parse.SaveCallback(){
                    @Override
                    public void done(ParseException e) {
                        if ( e == null )
                            return;
                        PrefManager.AppInfoPrefManager pmanager = new PrefManager.AppInfoPrefManager(MainApplication.instance());
                        pmanager.setAppSettingNeedSyncWithParse(true);
                    }
                });
            }catch (Exception ex){
                logger.error(ex);
            }
        }
    }

    public static boolean hasNotificationHash(Context context, String notificationId){
        PrefManager.AppInfoPrefManager pmanager = new PrefManager.AppInfoPrefManager(context);
        String prevHashCode = pmanager.getPrevNotificationHashKey();
        pmanager.setPrevNotificationHashKey(notificationId);
        if ( TextUtils.isEmpty(notificationId) && TextUtils.isEmpty(prevHashCode) )
            return true;
        if ( notificationId != null && notificationId.equals(prevHashCode) )
            return true;
        return false;
    }

}
