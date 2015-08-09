package org.edx.indonesiax.view;

import android.os.Bundle;
import android.webkit.WebView;

import org.edx.indonesiax.R;
import org.edx.indonesiax.base.FindCoursesBaseActivity;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_find_courses)
public class FindCoursesActivity extends FindCoursesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

        try{
            environment.getSegment().screenViewsTracking(getString(R.string.find_courses_title));
        }catch(Exception e){
            logger.error(e);
        }

        loadCourseSearchUrl();
    }

    @Override
    protected void onOnline() {
        super.onOnline();
        loadCourseSearchUrl();
    }

    private void loadCourseSearchUrl() {
        String url = environment.getConfig().getEnrollmentConfig().getCourseSearchUrl();
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.loadUrl(url);
    }
}
