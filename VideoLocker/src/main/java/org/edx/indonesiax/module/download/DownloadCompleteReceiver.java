package org.edx.indonesiax.module.download;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.edx.indonesiax.core.IEdxEnvironment;
import org.edx.indonesiax.event.DownloadEvent;
import org.edx.indonesiax.logger.Logger;
import org.edx.indonesiax.model.VideoModel;
import org.edx.indonesiax.model.db.DownloadEntry;
import org.edx.indonesiax.model.download.NativeDownloadModel;
import org.edx.indonesiax.module.analytics.ISegment;
import org.edx.indonesiax.module.db.DataCallback;
import org.edx.indonesiax.module.prefs.PrefManager;

import de.greenrobot.event.EventBus;
import roboguice.receiver.RoboBroadcastReceiver;

public class DownloadCompleteReceiver extends RoboBroadcastReceiver {
    private final Logger logger = new Logger(getClass().getName());

    @Inject
    private IEdxEnvironment environment;


    @Override
    protected void handleReceive(final Context context, Intent data){
        try {
            if (data != null && data.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID)) {
                long id = data.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id != -1) {
                    logger.debug("Received download notification for id: " + id);

                    // check if download was SUCCESSFUL
                    NativeDownloadModel nm = environment.getDownloadManager().getDownload(id);

                    if (nm == null || nm.status != DownloadManager.STATUS_SUCCESSFUL) {
                        logger.debug("Download seems failed or cancelled for id : " + id);
                        return;
                    } else {
                        logger.debug("Download successful for id : " + id);
                    }

                    // mark download as completed
                    environment.getStorage().markDownloadAsComplete(id, new DataCallback<VideoModel>() {
                        @Override
                        public void onResult(VideoModel result) {
                            if(result!=null){
                                DownloadEntry download = (DownloadEntry) result;

                                ISegment segIO = environment.getSegment();
                                segIO.trackDownloadComplete(download.videoId, download.eid,
                                        download.lmsUrl);

                                // update count of downloaded videos
                                // store user's data in his own preference file, so as to keep it unique
                                PrefManager p = new PrefManager(context, download.username);
                                long count = p.getLong(PrefManager.Key.COUNT_OF_VIDEOS_DOWNLOADED);
                                if (count < 0) {
                                    count = 0;
                                }
                                count ++;
                                p.put(PrefManager.Key.COUNT_OF_VIDEOS_DOWNLOADED, count);

                                EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.COMPLETED));
                            }
                        }

                        @Override
                        public void onFail(Exception ex) {
                            logger.error(ex);
                        }
                    });
                }
            }
        } catch(Exception ex) {
            logger.error(ex);
        }

    }

}
