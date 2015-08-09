package org.edx.indonesiax.base;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.indonesiax.R;
import org.edx.indonesiax.core.IEdxEnvironment;
import org.edx.indonesiax.logger.Logger;
import org.edx.indonesiax.model.api.ProfileModel;
import org.edx.indonesiax.module.prefs.PrefManager;
import org.edx.indonesiax.util.BrowserUtil;

import roboguice.fragment.RoboFragment;

public class CourseDetailBaseFragment extends RoboFragment {

   @Inject
    protected IEdxEnvironment environment;

    protected final Logger logger = new Logger(getClass().getName());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void showOpenInBrowserPanel(final String url) {
        if(TextUtils.isEmpty(url))
            return;

        try {
            final StringBuffer urlStringBuffer = new StringBuffer();
            if (!url.contains("http://") && !url.contains("https://")){
                urlStringBuffer.append("http://");
                urlStringBuffer.append(url);
            }else{
                urlStringBuffer.append(url);
            }

            if(getView()!=null){
                getView().findViewById(R.id.open_in_browser_panel).setVisibility(
                        View.VISIBLE);
                TextView openInBrowserTv = (TextView) getView().findViewById
                        (R.id.open_in_browser_btn);
                openInBrowserTv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new BrowserUtil().open(getActivity(),
                                urlStringBuffer.toString());
                    }
                });
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.debug("Error in hiding Open in Browser Panel");
        }
    }

    public void hideOpenInBrowserPanel() {
        try {
            if(getView()!=null){
                if(getView().findViewById(R.id.open_in_browser_panel)!=null){
                    getView().findViewById(R.id.open_in_browser_panel).setVisibility(
                            View.GONE);
                }
            }
        } catch (Exception ex) {
            logger.debug("Error in showing player");
            logger.error(ex);
        }
    }


    /**
     * Returns user's profile.
     * @return
     */
    protected ProfileModel getProfile() {
        PrefManager prefManager = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
        return prefManager.getCurrentUserProfile();
    }
}
