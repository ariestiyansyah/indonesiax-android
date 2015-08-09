package org.edx.indonesiax.task;

import android.content.Context;

import org.edx.indonesiax.model.db.DownloadEntry;
import org.edx.indonesiax.player.TranscriptManager;

import java.util.List;

public abstract class EnqueueDownloadTask extends Task<Long> {


    List<DownloadEntry> downloadList;
    public EnqueueDownloadTask(Context context, List<DownloadEntry> downloadList) {
        super(context);
        this.downloadList = downloadList;
    }

    @Override
    public Long call( ) throws Exception{
        try {

            if(downloadList!=null){
                int count = 0;
                for (DownloadEntry de : downloadList) {
                    try{
                        if(environment.getStorage().addDownload(de)!=-1){
                            count++;
                        }
                        TranscriptManager transManager = new TranscriptManager(context);
                        transManager.downloadTranscriptsForVideo(de.transcript);
                    }catch(Exception e){
                        logger.error(e);
                    }
                }
                return (long)count;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return 0L;
    }
}
