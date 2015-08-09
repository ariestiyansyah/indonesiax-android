package org.edx.indonesiax.model.course;

import org.edx.indonesiax.model.db.DownloadEntry;
import org.edx.indonesiax.module.storage.IStorage;

/**
 * Created by hanning on 5/20/15.
 */
public interface HasDownloadEntry {
    DownloadEntry getDownloadEntry(IStorage storage);
    long getSize();
}
