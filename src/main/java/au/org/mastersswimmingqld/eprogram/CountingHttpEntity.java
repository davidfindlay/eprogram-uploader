package au.org.mastersswimmingqld.eprogram;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by david on 27/03/2016.
 * Copied from http://stackoverflow.com/questions/7057342/how-to-get-a-progress-bar-for-a-file-upload-with-apache-httpclient-4
 */
public class CountingHttpEntity extends HttpEntityWrapper {

    private OutputStreamProgress outstream;
    private volatile long contentSize;
    private JProgressBar progressBar;

    public CountingHttpEntity(HttpEntity entity) {
        super(entity);
        contentSize = entity.getContentLength();
    }

    public CountingHttpEntity(HttpEntity entity, JProgressBar progressBar) {
        this(entity);
        setProgressBar(progressBar);
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        this.outstream = new OutputStreamProgress(outstream, contentSize, progressBar);
        super.writeTo(this.outstream);
    }
}
