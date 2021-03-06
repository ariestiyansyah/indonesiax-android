package org.edx.indonesiax.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.indonesiax.R;
import org.edx.indonesiax.core.IEdxEnvironment;
import org.edx.indonesiax.loader.AsyncTaskResult;
import org.edx.indonesiax.loader.FriendsInCourseLoader;
import org.edx.indonesiax.logger.Logger;
import org.edx.indonesiax.model.api.CourseEntry;
import org.edx.indonesiax.module.facebook.FacebookSessionUtil;
import org.edx.indonesiax.social.SocialMember;
import org.edx.indonesiax.util.BrowserUtil;
import org.edx.indonesiax.util.ResourceUtil;
import org.edx.indonesiax.util.UiUtil;
import org.edx.indonesiax.view.adapters.FriendsInCourseAdapter;
import org.edx.indonesiax.view.adapters.SimpleAdapter;
import org.edx.indonesiax.view.custom.EButton;

import java.util.List;

import roboguice.fragment.RoboFragment;

public class FriendsInCourseFragment extends RoboFragment implements LoaderManager.LoaderCallbacks<AsyncTaskResult<List<SocialMember>>> {

    private static final String TAG = FriendsInCourseFragment.class.getCanonicalName();
    public static final String ARG_COURSE = TAG + ".argCourse";
    public static final String ARG_SHOW_COURSE_LINK = TAG + ".argShowCourseLink";

    private final Logger logger = new Logger(FriendsInCourseFragment.class);

    private SimpleAdapter<SocialMember> adapter;
    private CourseEntry courseData;
    private boolean showCourseLink;

    private ProgressBar progressBar;
    private TextView courseLabel;
    private TextView errorLabel;
    private SwipeRefreshLayout listContainer;

    private final int LOADER_ID = 0x416EEE;

    @Inject
    IEdxEnvironment environment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null || !args.containsKey(ARG_COURSE)) {
            throw new IllegalArgumentException("missing args");
        }
        courseData = (CourseEntry) args.getSerializable(ARG_COURSE);
        showCourseLink = args.getBoolean(ARG_SHOW_COURSE_LINK);

        if (adapter == null) {
            adapter = new FriendsInCourseAdapter(getActivity());
        }

        try{
            environment.getSegment().screenViewsTracking("Friends In This Course");
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_in_course, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.member_list);
        listView.setAdapter(adapter);

        listContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        listContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                fetchCourseMembers();

            }
        });

        courseLabel = (TextView) rootView.findViewById(R.id.course_label);
        String content = ResourceUtil.getFormattedString(R.string.friends_in_course, "courseName", courseData.getName()).toString();
        courseLabel.setText( content );

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        errorLabel = (TextView) rootView.findViewById(R.id.label_error);

        EButton courseBtn = (EButton) rootView.findViewById(R.id.btn_open_public_course);

        if (showCourseLink) {

            courseBtn.setVisibility(View.VISIBLE);
            courseBtn.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new BrowserUtil().open(getActivity(), courseData.getCourse_url());

                }
            });
            courseLabel.setText(R.string.friends_in_this_course);

        }

        return rootView;
    }

    private void fetchCourseMembers(){

        Bundle args = new Bundle();
        args.putString(FriendsInCourseLoader.TAG_COURSE_ID, courseData.getId());
        args.putString(FriendsInCourseLoader.TAG_COURSE_OAUTH, FacebookSessionUtil.getAccessToken());

        getLoaderManager().restartLoader(LOADER_ID, args, this);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void onRefreshed(List<SocialMember> members) {

        progressBar.setVisibility(View.GONE);
        listContainer.setRefreshing(false);

        adapter.setItems(members);
        if (adapter.isEmpty()) {
            courseLabel.setVisibility(View.GONE);
            listContainer.setVisibility(View.GONE);
            errorLabel.setVisibility(View.VISIBLE);
        } else {
            courseLabel.setVisibility(View.VISIBLE);

            listContainer.setVisibility(View.VISIBLE);
            errorLabel.setVisibility(View.GONE);
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (courseData != null && courseData.getMembers_list() != null){
            onRefreshed(courseData.getMembers_list());
        } else {
            //Members list isn't loaded yet.
            fetchCourseMembers();
        }

    }

    @Override
    public Loader<AsyncTaskResult<List<SocialMember>>> onCreateLoader(int i, Bundle bundle) {
        return new FriendsInCourseLoader(getActivity(), bundle, environment);
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<List<SocialMember>>> objectLoader, AsyncTaskResult<List<SocialMember>> result) {

        if (result.getResult() != null) {
            courseData.setMembers_list(result.getResult());
            onRefreshed(courseData.getMembers_list());
        } else {
            //TODO Handle error
            UiUtil.showMessage(getView(), getString(R.string.error_friends_list));
        }


    }

    @Override
    public void onLoaderReset(Loader<AsyncTaskResult<List<SocialMember>>> objectLoader) {
        errorLabel.setVisibility(View.GONE);
    }

}
