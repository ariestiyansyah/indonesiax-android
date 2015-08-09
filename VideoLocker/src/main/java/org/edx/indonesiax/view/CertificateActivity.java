package org.edx.indonesiax.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.indonesiax.R;
import org.edx.indonesiax.base.BaseSingleFragmentActivity;
import org.edx.indonesiax.model.api.EnrolledCoursesResponse;

public class CertificateActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.tab_label_certificate));
    }

    @Override
    public Fragment getFirstFragment() {

        Fragment frag = new CertificateFragment();

        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) getIntent().getSerializableExtra(CertificateFragment.ENROLLMENT);
        if (courseData != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(CertificateFragment.ENROLLMENT, courseData);
            frag.setArguments(bundle);

        }

        return frag;
    }

}
