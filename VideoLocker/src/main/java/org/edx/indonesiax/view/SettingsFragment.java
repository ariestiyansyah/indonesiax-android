package org.edx.indonesiax.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;
import com.google.inject.Inject;

import org.edx.indonesiax.R;
import org.edx.indonesiax.core.IEdxEnvironment;
import org.edx.indonesiax.loader.AsyncTaskResult;
import org.edx.indonesiax.loader.CoursesVisibleLoader;
import org.edx.indonesiax.logger.Logger;
import org.edx.indonesiax.module.facebook.IUiLifecycleHelper;
import org.edx.indonesiax.module.prefs.PrefManager;
import org.edx.indonesiax.social.SocialMember;
import org.edx.indonesiax.social.SocialProvider;
import org.edx.indonesiax.social.facebook.FacebookProvider;
import org.edx.indonesiax.util.ResourceUtil;
import org.edx.indonesiax.util.SocialUtils;
import org.edx.indonesiax.view.custom.ETextView;
import org.edx.indonesiax.view.dialog.IDialogCallback;
import org.edx.indonesiax.view.dialog.NetworkCheckDialogFragment;

import java.util.Arrays;

import roboguice.fragment.RoboFragment;

public class SettingsFragment extends RoboFragment implements LoaderManager.LoaderCallbacks<AsyncTaskResult<Boolean>>{

    public static final String TAG = SettingsFragment.class.getCanonicalName();
    private static final int SHARE_LOADER_ID = 0x0000f0f0;

    private final Logger logger = new Logger(SettingsFragment.class);

    private NetworkCheckDialogFragment newFragment;
    private IUiLifecycleHelper uiHelper;
    private Switch wifiSwitch;
    private Switch visibilitySwitch;
    @Inject
    protected IEdxEnvironment environment;
    boolean showSocialFeatures;

    private ETextView socialConnectedText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try{
            environment.getSegment().screenViewsTracking("Settings");
        }catch(Exception e){
            logger.error(e);
        }

        PrefManager featurePrefManager = new PrefManager(getActivity(), PrefManager.Pref.FEATURES);
        showSocialFeatures = featurePrefManager.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, true);

        uiHelper = IUiLifecycleHelper.Factory.getInstance(getActivity(), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (showSocialFeatures) {
                    onFacebookStatusChange(state.isOpened());
                }
            }
        });
        uiHelper.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_settings, container, false);

        wifiSwitch = (Switch) layout.findViewById(R.id.wifi_setting);
        visibilitySwitch = (Switch) layout.findViewById(R.id.course_visibility_setting);

        updateWifiSwitch();
        updateVisibilitySwitch();

        LinearLayout socialView = (LinearLayout) layout.findViewById(R.id.settings_social_layout);

        if (showSocialFeatures) {

            LoginButton loginButton = (LoginButton) layout.findViewById(R.id.authButton);
            loginButton.setReadPermissions(Arrays.asList("user_likes", "user_status", "user_friends"));
            loginButton.setFragment(this);

            socialConnectedText = (ETextView) layout.findViewById(R.id.settings_fb_login_body);

            SocialProvider socialProvider = new FacebookProvider();
            if (socialProvider.isLoggedIn()) {
                populateLoginText();
            } else {
                socialConnectedText.setText(R.string.settings_facebook_login_body_logged_out);
                loginButton.setText(R.string.settings_facebook_login_btn);
            }
            socialView.setVisibility(View.VISIBLE);

        } else {

            socialView.setVisibility(View.GONE);

        }
//        uiHelper = new UiLifecycleHelper(getActivity(), facebookStatusCallback);
//        uiHelper.onCreate(savedInstanceState);

        Bundle args = new Bundle();
        args.putBoolean(CoursesVisibleLoader.KEY_GET_VALUE, true);
        getLoaderManager().initLoader(SHARE_LOADER_ID, args, this);

        return layout;

    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        uiHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void onFacebookStatusChange(boolean isLoggedIn) {

        if (isLoggedIn) {

            populateLoginText();

        } else {

            socialConnectedText.setText(R.string.settings_facebook_login_body_logged_out);
        }

        setCourseVisibilitySwitchEnabled(isLoggedIn);

        try{
            environment.getSegment().socialConnectionEvent(isLoggedIn, SocialUtils.Values.FACEBOOK);
        }catch(Exception e){
            logger.error(e);
        }

    }

    private void populateLoginText() {

        socialConnectedText.setText(R.string.settings_facebook_login_fetch);
        SocialProvider socialProvider = new FacebookProvider();
        socialProvider.getUser(getActivity(), new SocialProvider.Callback<SocialMember>() {

            @Override
            public void onSuccess(SocialMember response) {
                if(!isAdded())
                    return;
                CharSequence formatted =  ResourceUtil.getFormattedString(R.string.settings_facebook_login_body_logged_in,
                         "username", response.getFullName());
                socialConnectedText.setText(formatted);
                socialConnectedText.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(SocialProvider.SocialError err) {
                if(!isAdded())
                    return;

                socialConnectedText.setText(R.string.settings_facebook_login_error);
            }

        });

    }

    private void updateVisibilitySwitch(){
        PrefManager prefs = new PrefManager(getActivity(), PrefManager.Pref.FEATURES);
        boolean doesShare = prefs.getBoolean(PrefManager.Key.SHARE_COURSES, false);
        visibilitySwitch.setChecked(doesShare);
    }

    private void updateWifiSwitch() {
        final PrefManager wifiPrefManager = new PrefManager(
                getActivity().getBaseContext(), PrefManager.Pref.WIFI);

        wifiSwitch.setOnCheckedChangeListener(null);
        wifiSwitch.setChecked(wifiPrefManager.getBoolean(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI,true));
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, true);
                }else{
                    showWifiDialog();
                }
            }
        });
    }

    protected void showWifiDialog() {

        newFragment = NetworkCheckDialogFragment.newInstance(getString(R.string.wifi_dialog_title_help),
                getString(R.string.wifi_dialog_message_help),
                new IDialogCallback() {
            @Override
            public void onPositiveClicked() {
                try {
                    PrefManager wifiPrefManager = new PrefManager
                            (getActivity().getBaseContext(), PrefManager.Pref.WIFI);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, false);
                    updateWifiSwitch();
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }

            @Override
            public void onNegativeClicked() {
                try {
                    PrefManager wifiPrefManager = new PrefManager(
                            getActivity().getBaseContext(), PrefManager.Pref.WIFI);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, true);

                    updateWifiSwitch();
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        });

        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
        newFragment.setCancelable(false);
    }

    @Override
    public Loader<AsyncTaskResult<Boolean>> onCreateLoader(int id, Bundle args) {
        return new CoursesVisibleLoader(getActivity(), args, environment);
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<Boolean>> loader, AsyncTaskResult<Boolean> data) {
        if(data.getEx() != null){
        }
        else if(data.getResult() != null) {

            //store local cache of result
            PrefManager prefs = new PrefManager(getActivity(), PrefManager.Pref.FEATURES);
            prefs.put(PrefManager.Key.SHARE_COURSES, data.getResult());

            updateVisibilitySwitch();

            visibilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton button, boolean isChecked) {

                    try {
                        environment.getSegment().coursesVisibleToFriendsChange(isChecked);
                    } catch (Exception e) {
                        logger.error(e);
                    }

                    Bundle args = new Bundle();
                    args.putBoolean(CoursesVisibleLoader.KEY_SET_TO_VALUE, isChecked);
                    getLoaderManager().restartLoader(SHARE_LOADER_ID, args, SettingsFragment.this);

                }
            });

            setCourseVisibilitySwitchEnabled(true);
        }
    }

    private void setCourseVisibilitySwitchEnabled(boolean enabled){

        visibilitySwitch.setEnabled(enabled);

        if(enabled) {
            ((TextView) getView().findViewById(R.id.visibilitytext)).setTextColor(getResources().getColor(R.color.grey_6));
            ((TextView) getView().findViewById(R.id.make_course_visible_text)).setTextColor(getResources().getColor(R.color.grey_4));
        } else{
            ((TextView) getView().findViewById(R.id.visibilitytext)).setTextColor(getResources().getColor(R.color.grey_4));
            ((TextView) getView().findViewById(R.id.make_course_visible_text)).setTextColor(getResources().getColor(R.color.grey_2));
        }

    }

    @Override
    public void onLoaderReset(Loader<AsyncTaskResult<Boolean>> loader) {

    }
}
