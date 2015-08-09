package org.edx.indonesiax.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.edx.indonesiax.R;
import org.edx.indonesiax.base.MainApplication;
import org.edx.indonesiax.logger.Logger;
import org.edx.indonesiax.model.course.BlockPath;
import org.edx.indonesiax.model.course.CourseComponent;
import org.edx.indonesiax.module.prefs.PrefManager;
import org.edx.indonesiax.services.LastAccessManager;
import org.edx.indonesiax.services.VideoDownloadHelper;
import org.edx.indonesiax.util.AppConstants;

/**
 * Created by hanning on 5/15/15.
 */
public abstract class CourseVideoListActivity  extends CourseBaseActivity implements
    LastAccessManager.LastAccessManagerCallback ,VideoDownloadHelper.DownloadManagerCallback {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private boolean isFetchingLastAccessed;
    private Handler mHandler = new Handler();


    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
    }




    @Override
    public boolean isFetchingLastAccessed() {
        return isFetchingLastAccessed;
    }

    @Override
    public void setFetchingLastAccessed(boolean accessed) {
        this.isFetchingLastAccessed = accessed;
    }

    private long lastClickTime;
    @Override
    public void showLastAccessedView(final String lastAccessedSubSectionId, final String courseId, final View view) {
        if (  isActivityStarted() ) {
            if (!AppConstants.offline_flag) {
                try {
                    if(courseId!=null && lastAccessedSubSectionId!=null){
                        final CourseComponent lastAccessComponent = courseManager.getComponentById(courseId, lastAccessedSubSectionId);
                        if (lastAccessComponent != null) {
                            //if last access section has no video and app is on video-only model,
                            //we should hide last-access-view for now.  TODO - i believe it is a temporary solution. we should
                            //get rid of video-only mode in the future?
                            PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(MainApplication.instance());
                            if ( userPrefManager.isUserPrefVideoModel() &&
                                lastAccessComponent.getVideos().isEmpty() )
                                return;

                            super.showLastAccessedView(null, " " + lastAccessComponent.getName(), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //This has been used so that if user clicks continuously on the screen,
                                    //two activities should not be opened
                                    long currentTime = SystemClock.elapsedRealtime();
                                    if (currentTime - lastClickTime > 1000) {
                                        lastClickTime = currentTime;
                                        try {
                                            environment.getRouter().showCourseContainerOutline(
                                                CourseVideoListActivity.this, courseData, lastAccessedSubSectionId);
                                        } catch (Exception e) {
                                            logger.error(e);
                                        }
                                    }
                                }
                            });
                        } else {
                            hideLastAccessedView(view);
                        }
                    }
                } catch (Exception e) {
                    hideLastAccessedView(view);
                    logger.error(e);
                }
            } else {
                hideLastAccessedView(view);
            }
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    protected void modeChanged(){

        if (isOnCourseOutline())
            LastAccessManager.getSharedInstance().fetchLastAccessed(this, courseData.getCourse().getId());

        updateListUI();
    }

    @Override
    protected void updateDownloadProgress(final int progressPercent){

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressPercent < 100) {
                    downloadProgressBar.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacks(mHideRunnable);
                    if (downloadIndicator.getAnimation() == null) {
                        downloadIndicator.setVisibility(View.VISIBLE);
                        Animation animation = AnimationUtils.loadAnimation(CourseVideoListActivity.this, R.anim.rotate);
                        downloadIndicator.startAnimation(animation);
                    }
                } else { //progressPercent == 100
                    downloadIndicator.clearAnimation();
                    downloadIndicator.setVisibility(View.INVISIBLE);
                    mHandler.postDelayed(mHideRunnable, getResources().getInteger(R.integer.message_delay));
                }
            }
        }, 500);
    }

    @Override
    protected void setVisibilityForDownloadProgressView(boolean show){
         boolean visible = downloadProgressBar.getVisibility() == View.VISIBLE;
        if (visible == show )
            return; //do nothing

        if ( show ){
            //TODO - we pass a value less than 100 to indicate it is downloading.
            updateDownloadProgress(0);
        } else {
            updateDownloadProgress(100);
        }
    }

    @Override
    public void onDownloadSuccess(Long result) {
        try {
            updateListUI();
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void onDownloadFailure() {
    }

    @Override
    public void showProgressDialog(int numDownloads) {
        setVisibilityForDownloadProgressView(true);
    }

    @Override
    public abstract  void updateListUI();


    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            downloadProgressBar.setVisibility(  View.GONE );
        }
    };

    //TODO - legacy code use one minute tick loop to sync some UI status, like
    //total download progress. this is a simple approach, but may not be the
    //best one.
    protected void onTick() {
        // this is a per second callback
        try {
                if(AppConstants.offline_flag){
                    setVisibilityForDownloadProgressView(false);
                }else{
                    if(environment.getDatabase()!=null){
                        boolean downloading = environment.getDatabase().isAnyVideoDownloading(null);
                        if(!downloading){
                            setVisibilityForDownloadProgressView(false);
                        }else{
                            environment.getStorage().getAverageDownloadProgress(averageProgressCallback);
                        }
                    }   //store not null check
                }
            }  catch(Exception ex) {
            logger.error(ex);
        }
    }

    protected boolean isOnCourseOutline(){
        CourseComponent outlineComp = courseManager.getComponentById(
                courseData.getCourse().getId(), courseComponentId);
        BlockPath outlinePath = outlineComp.getPath();
        int outlinePathSize = outlinePath.getPath().size();

        return outlinePathSize <= 1;
    }

}


