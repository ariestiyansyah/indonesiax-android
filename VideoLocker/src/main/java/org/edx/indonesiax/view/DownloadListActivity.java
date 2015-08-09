package org.edx.indonesiax.view;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.edx.indonesiax.R;
import org.edx.indonesiax.base.BaseFragmentActivity;
import org.edx.indonesiax.model.VideoModel;
import org.edx.indonesiax.model.db.DownloadEntry;
import org.edx.indonesiax.module.db.DataCallback;
import org.edx.indonesiax.util.AppConstants;
import org.edx.indonesiax.util.NetworkUtil;
import org.edx.indonesiax.view.adapters.DownloadEntryAdapter;

import java.util.ArrayList;
import java.util.List;

public class DownloadListActivity extends BaseFragmentActivity {

    private static final int MSG_UPDATE_PROGRESS = 1022;
    private DownloadEntryAdapter adapter;
    private View offlineBar;
    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_UPDATE_PROGRESS) {
                if (isActivityStarted()) {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                        logger.debug("download list reloaded");
                    }
                    sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 3000);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads_list);

        handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 0);

        try{
            environment.getSegment().screenViewsTracking(getString
                    (R.string.title_download));
        }catch(Exception e){
            logger.error(e);
        }


        offlineBar = (View) findViewById(R.id.offline_bar);
        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            if (offlineBar != null)
                offlineBar.setVisibility(View.VISIBLE);
        }

        ListView downloadListView = (ListView) findViewById(R.id.my_downloads_list);
        adapter = new DownloadEntryAdapter(this, environment) {

            @Override
            public void onItemClicked(DownloadEntry model) {
                // nothing to do here
            }

            @Override
            public void onDownloadComplete(DownloadEntry model) {
                //showDownloadCompleteView();
                adapter.remove(model);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onDeleteClicked(DownloadEntry model) {
                if(environment.getStorage().removeDownload(model) >= 1){
                    // update the list data as one download is removed
                    try{
                        adapter.remove(model);
                        adapter.notifyDataSetChanged();
                    }catch(Exception e){
                        logger.error(e);
                    }

                }
            }
        };

        adapter.setStore(environment.getStorage());
        downloadListView.setAdapter(adapter);
        final ArrayList<DownloadEntry> list = new ArrayList<DownloadEntry>();
        environment.getDatabase().getListOfOngoingDownloads(new DataCallback<List<VideoModel>>() {
            @Override
            public void onResult(List<VideoModel> result) {
                if(result!=null){
                    for(VideoModel de : result){
                        list.add((DownloadEntry) de);
                    }
                    adapter.setItems(list);
                }
            }
            @Override
            public void onFail(Exception ex) {
                logger.error(ex);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            ActionBar bar = getActionBar();
            if (bar != null) {
                bar.show();
            }
            setTitle(getString(R.string.title_download));
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 0);
        invalidateOptionsMenu();
    };


    @Override
    protected void onOffline() {
        AppConstants.offline_flag = true;
        offlineBar.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    @Override
    protected void onOnline() {
        AppConstants.offline_flag = false;
        offlineBar.setVisibility(View.GONE);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // inflate menu from xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem menuItem = menu.findItem(R.id.progress_download);
        menuItem.setVisible(false);

        MenuItem offline_tvItem = menu.findItem(R.id.offline);
        if (AppConstants.offline_flag) {
            offline_tvItem.setVisible(true);
        } else {
            offline_tvItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.progress_download);
        menuItem.setVisible(false);

        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);
        checkBox_menuItem.setVisible(false);

        return true;
    }

}
