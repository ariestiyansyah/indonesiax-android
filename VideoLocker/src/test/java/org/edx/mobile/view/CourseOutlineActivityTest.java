package org.edx.indonesiax.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.indonesiax.model.api.EnrolledCoursesResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.util.ActivityController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(RobolectricGradleTestRunner.class)
public class CourseOutlineActivityTest extends CourseBaseActivityTest {
    /**
     * Method for defining the subclass of {@link CourseOutlineActivity} that
     * is being tested. Should be overridden by subclasses.
     *
     * @return The {@link CourseOutlineActivity} subclass that is being tested
     */
    @Override
    protected Class<? extends CourseOutlineActivity> getActivityClass() {
        return CourseOutlineActivity.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean appliesPrevTransitionOnRestart() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void initializeTest() {
        super.initializeTest();

        ActivityController<? extends CourseOutlineActivity> controller =
                Robolectric.buildActivity(getActivityClass());
        CourseOutlineActivity activity = controller.get();

        EnrolledCoursesResponse courseData = new EnrolledCoursesResponse();
        String courseComponentId = "id";
        Bundle data  = new Bundle();
        data.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
        data.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);

        controller.create(data).postCreate(null);
        Fragment fragment = activity.getSupportFragmentManager()
                .findFragmentByTag(CourseOutlineFragment.TAG);
        assertNotNull(fragment);
        assertThat(fragment).isInstanceOf(CourseOutlineFragment.class);
        assertTrue(fragment.getRetainInstance());
        Bundle args = fragment.getArguments();
        assertNotNull(args);
        assertEquals(data.getSerializable(Router.EXTRA_ENROLLMENT),
                args.getSerializable(Router.EXTRA_ENROLLMENT));
        assertEquals(data.getSerializable(Router.EXTRA_COURSE_COMPONENT_ID),
                args.getSerializable(Router.EXTRA_COURSE_COMPONENT_ID));
    }
}
