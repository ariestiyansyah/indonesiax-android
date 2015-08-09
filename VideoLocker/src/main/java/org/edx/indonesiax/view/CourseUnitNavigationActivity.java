package org.edx.indonesiax.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.indonesiax.R;
import org.edx.indonesiax.base.MainApplication;
import org.edx.indonesiax.logger.Logger;
import org.edx.indonesiax.model.course.BlockType;
import org.edx.indonesiax.model.course.CourseComponent;
import org.edx.indonesiax.model.course.HtmlBlockModel;
import org.edx.indonesiax.model.course.VideoBlockModel;
import org.edx.indonesiax.module.prefs.PrefManager;
import org.edx.indonesiax.services.ViewPagerDownloadManager;
import org.edx.indonesiax.view.common.PageViewStateCallback;
import org.edx.indonesiax.view.custom.DisableableViewPager;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import roboguice.inject.InjectView;


/**
 *
 */
public class CourseUnitNavigationActivity extends CourseBaseActivity implements CourseUnitVideoFragment.HasComponent {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private DisableableViewPager pager;
    private CourseComponent selectedUnit;

    private List<CourseComponent> unitList = new ArrayList<>();
    private CourseUnitPagerAdapter pagerAdapter;

    @InjectView(R.id.goto_next)
    private Button mNextBtn;
    @InjectView(R.id.goto_prev)
    private Button mPreviousBtn;
    @InjectView(R.id.next_unit_title)
    private TextView mNextUnitLbl;
    @InjectView(R.id.prev_unit_title)
    private TextView mPreviousUnitLbl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout insertPoint = (RelativeLayout)findViewById(R.id.fragment_container);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.view_course_unit_pager, null);
        insertPoint.addView(v, 0,
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        pager = (DisableableViewPager)findViewById(R.id.pager);
        pagerAdapter = new CourseUnitPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);


        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if ( state == ViewPager.SCROLL_STATE_DRAGGING ){
                    int curIndex = pager.getCurrentItem();
                    PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, curIndex);
                    if( curView != null )
                        curView.onPageDisappear();
                }
                if ( state == ViewPager.SCROLL_STATE_IDLE ){
                    int curIndex = pager.getCurrentItem();
                    PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, curIndex);
                     if( curView != null )
                         curView.onPageShow();
                    tryToUpdateForEndOfSequential();
                }
            }
        });

        findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);

        mPreviousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pager.getCurrentItem();
                if (index > 0) {
                    PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, index);
                    if (curView != null)
                        curView.onPageDisappear();
                    pager.setCurrentItem(index - 1);
                }
            }
        });
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pager.getCurrentItem();
                if (index < pagerAdapter.getCount() - 1) {
                    PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, index);
                    if (curView != null)
                        curView.onPageDisappear();
                    pager.setCurrentItem(index + 1);
                }
            }
        });

        updateUIForOrientation();

        updateDataModel();

        setApplyPrevTransitionOnRestart(true);

        try{
            environment.getSegment().screenViewsTracking("Assessment");
        }catch(Exception e){
            logger.error(e);
        }

    }


    protected  String getUrlForWebView(){
        if ( selectedUnit == null ){
            return ""; //wont happen
        } else {
            return selectedUnit.getWebUrl();
        }
    }

    private void setCurrentUnit(CourseComponent component){
        this.selectedUnit = component;
        if ( this.selectedUnit == null  )
            return;

        environment.getDatabase().updateAccess(null, selectedUnit.getId(), true);

        CourseComponent parent = component.getParent();
        String prefName = PrefManager.getPrefNameForLastAccessedBy(getProfile()
            .username, selectedUnit.getCourseId());
        final PrefManager prefManager = new PrefManager(MainApplication.instance(), prefName);
        prefManager.putLastAccessedSubsection(parent.getId(), false);
        Intent resultData = new Intent();
        resultData.putExtra(Router.EXTRA_COURSE_UNIT, selectedUnit);
        setResult(RESULT_OK, resultData);
    }

    private void tryToUpdateForEndOfSequential(){
        int curIndex = pager.getCurrentItem();
        setCurrentUnit(pagerAdapter.getUnit(curIndex));

        mPreviousBtn.setEnabled(curIndex > 0);
        mNextBtn.setEnabled(curIndex < pagerAdapter.getCount() - 1);

        findViewById(R.id.course_unit_nav_bar).requestLayout();

        setTitle(selectedUnit.getDisplayName());

        // fix: https://openedx.atlassian.net/browse/MA-995
        // code below decides to show Next/Previous Unit name or only Next/Previous
        // based on units in a subsection
        String currentSubsectionId = selectedUnit.getParent().getId();
        if (curIndex + 1 <= pagerAdapter.getCount() - 1) {
            String nextUnitSubsectionId = unitList.get(curIndex + 1).getParent().getId();
            if (currentSubsectionId.equalsIgnoreCase(nextUnitSubsectionId)) {
                mNextUnitLbl.setVisibility(View.GONE);
                mNextBtn.setText(R.string.assessment_next);
            }
            else {
                mNextUnitLbl.setText(unitList.get(curIndex + 1).getParent().getName());
                mNextUnitLbl.setVisibility(View.VISIBLE);
                mNextBtn.setText(R.string.assessment_next_unit);
            }
        }
        else {
            // we have reached the end and next button is disabled
            mNextBtn.setText(R.string.assessment_next);
            mNextUnitLbl.setVisibility(View.GONE);
        }

        if (curIndex - 1 >= 0) {
            String prevUnitSubsectionId = unitList.get(curIndex - 1).getParent().getId();
            if (currentSubsectionId.equalsIgnoreCase(prevUnitSubsectionId)) {
                mPreviousUnitLbl.setVisibility(View.GONE);
                mPreviousBtn.setText(R.string.assessment_previous);
            }
            else {
                mPreviousUnitLbl.setText(unitList.get(curIndex - 1).getParent().getName());
                mPreviousUnitLbl.setVisibility(View.VISIBLE);
                mPreviousBtn.setText(R.string.assessment_previous_unit);
            }
        }
        else {
            // we have reached the start and previous button is disabled
            mPreviousBtn.setText(R.string.assessment_previous);
            mPreviousUnitLbl.setVisibility(View.GONE);
        }
    }

    protected void initialize(Bundle arg){
        super.initialize(arg);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if( selectedUnit != null )
            outState.putSerializable(Router.EXTRA_COURSE_UNIT, selectedUnit);
        super.onSaveInstanceState(outState);
    }

    protected void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            setCurrentUnit((CourseComponent) savedInstanceState.getSerializable(Router.EXTRA_COURSE_UNIT) );
        }
        super.restore(savedInstanceState);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    protected void onResume() {
        super.onResume();

    }


    private void updateDataModel(){
        unitList.clear();
        if( selectedUnit == null || selectedUnit.getRoot() == null ) {
            logger.warn("selectedUnit is null?");
            return;   //should not happen
        }

        //if we want to navigate through all unit of within the parent node,
        //we should use courseComponent instead.   Requirement maybe changed?
       // unitList.addAll( courseComponent.getChildLeafs() );
        List<CourseComponent> leaves = new ArrayList<>();

        PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(MainApplication.instance());
        EnumSet<BlockType> types =  userPrefManager.isUserPrefVideoModel() ?
                             EnumSet.of(BlockType.VIDEO) : EnumSet.allOf(BlockType.class);
        ((CourseComponent) selectedUnit.getRoot()).fetchAllLeafComponents(leaves, types);
        unitList.addAll( leaves );

        ViewPagerDownloadManager.instance.setMainComponent(selectedUnit, unitList);

        int index = unitList.indexOf(selectedUnit);
        if ( index >= 0 ){
            pager.setCurrentItem( index );
            tryToUpdateForEndOfSequential();
        }

        if ( pagerAdapter  != null )
            pagerAdapter.notifyDataSetChanged();

    }

    protected void modeChanged(){
        onBackPressed();
    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUIForOrientation();
    }

    private void updateUIForOrientation(){
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setActionBarVisible(false);
            findViewById(R.id.course_unit_nav_bar).setVisibility(View.GONE);
            pager.setPagingEnabled(false);
        } else {
            setActionBarVisible(true);
            findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);
            pager.setPagingEnabled(true);
        }
    }

    private class CourseUnitPagerAdapter extends FragmentStatePagerAdapter {

        public CourseUnitPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        public CourseComponent getUnit(int pos){
            if ( pos >= unitList.size() )
                pos = unitList.size() -1;
            if ( pos < 0 )
                pos = 0;
            return unitList.get(pos);
        }
        @Override
        public Fragment getItem(int pos) {
            CourseComponent unit = getUnit(pos);
            CourseUnitFragment unitFragment = null;
            //FIXME - for the video, let's ignore responsive_UI for now
            if ( unit instanceof VideoBlockModel) {
                CourseUnitVideoFragment fragment = CourseUnitVideoFragment.newInstance((VideoBlockModel)unit);

                unitFragment = fragment;
            }

            else if ( !unit.isResponsiveUI() ){
                unitFragment = CourseUnitMobileNotSupportedFragment.newInstance(unit);
            }

            else if ( unit.getType() != BlockType.VIDEO &&
                unit.getType() != BlockType.HTML &&
                unit.getType() != BlockType.OTHERS &&
                unit.getType() != BlockType.DISCUSSION &&
                unit.getType() != BlockType.PROBLEM ) {
                unitFragment = CourseUnitEmptyFragment.newInstance(unit);
            }

            else if ( unit instanceof HtmlBlockModel ){
                unitFragment = CourseUnitWebviewFragment.newInstance((HtmlBlockModel)unit);
            }

            //fallback
            else {
                unitFragment = CourseUnitMobileNotSupportedFragment.newInstance(unit);
            }

            unitFragment.setHasComponentCallback(CourseUnitNavigationActivity.this);

            return unitFragment;
        }

        @Override
        public int getCount() {
            return unitList.size();
        }
    }

    public CourseComponent getComponent() {
        return selectedUnit;
    }
    /// we won't show download and last access view here
    protected void setVisibilityForDownloadProgressView(boolean show){  }

    protected void hideLastAccessedView(View v) { }

    protected void showLastAccessedView(View v, String title, View.OnClickListener listener) {}

}
