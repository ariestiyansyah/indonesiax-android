package org.edx.indonesiax.util;

import android.content.Context;

import com.google.inject.Inject;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.edx.indonesiax.logger.Logger;
import org.edx.indonesiax.services.ServiceManager;

import java.io.IOException;

public abstract class TranscriptDownloader implements Runnable {

    private String srtUrl;
    private Context context;
    @Inject
    ServiceManager localApi;
    private final Logger logger = new Logger(TranscriptDownloader.class.getName());

    public TranscriptDownloader(Context context, String url) {
        this.srtUrl = url;
        this.context = context;
    }

    @Override
    public void run() {

        try {
            String response = localApi.downloadTranscript(srtUrl);
            onDownloadComplete(response);
        } catch (ParseException localParseException) {
            handle(localParseException);
            logger.error(localParseException);
        } catch (ClientProtocolException localClientProtocolException) {
            handle(localClientProtocolException);
            logger.error(localClientProtocolException);
        } catch (IOException localIOException) {
            handle(localIOException);
            logger.error(localIOException);
        } catch (Exception localException) {
            handle(localException);
            logger.error(localException);
        }
    }

    public abstract void handle(Exception ex);

    public abstract void onDownloadComplete(String response);
}
