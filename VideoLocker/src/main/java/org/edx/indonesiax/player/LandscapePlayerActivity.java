package org.edx.indonesiax.player;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import org.edx.indonesiax.R;
import org.edx.indonesiax.base.BaseFragmentActivity;

public class LandscapePlayerActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landscape_player);

        // this is to lock to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
}
