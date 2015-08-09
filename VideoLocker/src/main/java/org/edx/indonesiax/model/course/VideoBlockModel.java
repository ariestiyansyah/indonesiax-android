package org.edx.indonesiax.model.course;

import org.edx.indonesiax.model.db.DownloadEntry;
import org.edx.indonesiax.module.storage.IStorage;

/**
 * common base class for all type of units
 */
public class VideoBlockModel extends CourseComponent implements HasDownloadEntry {

    private DownloadEntry downloadEntry;
    private VideoData data;

    public VideoBlockModel(BlockModel blockModel, CourseComponent parent){
        super(blockModel,parent);
        this.data = (VideoData)blockModel.data;
    }

    public DownloadEntry getDownloadEntry(IStorage storage) {
        if ( storage != null ) {
            downloadEntry = (DownloadEntry) storage
                .getDownloadEntryfromVideoModel(this);
        }
        return downloadEntry;
    }

    public long getSize(){
         return data.encodedVideos.getPreferredVideoInfo().fileSize;
    }

    public VideoData getData() {
        return data;
    }

    public void setData(VideoData data) {
        this.data = data;
    }


}
