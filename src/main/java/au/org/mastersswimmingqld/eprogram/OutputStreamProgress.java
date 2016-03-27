package au.org.mastersswimmingqld.eprogram;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by david on 27/03/2016.
 * Copied from http://stackoverflow.com/questions/7057342/how-to-get-a-progress-bar-for-a-file-upload-with-apache-httpclient-4
 */
public class OutputStreamProgress extends OutputStream {

    private final OutputStream outstream;
    private volatile long bytesWritten = 0;
    private volatile long contentSize;
    private JProgressBar progressBar;

    public OutputStreamProgress(OutputStream outstream) {
        this.outstream = outstream;
    }

    public OutputStreamProgress(OutputStream outstream, long contentSize) {
        this(outstream);
        this.contentSize = contentSize;
    }

    public OutputStreamProgress(OutputStream outstream, long contentSize, JProgressBar progressBar) {
        this(outstream, contentSize);
        setProgressBar(progressBar);
        progressBar.setStringPainted(true);
    }

    @Override
    public void write(int b) throws IOException {
        outstream.write(b);
        bytesWritten++;
        updateProgressBar();
    }

    @Override
    public void write(byte[] b) throws IOException {
        outstream.write(b);
        bytesWritten += b.length;
        updateProgressBar();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outstream.write(b, off, len);
        bytesWritten += len;
        updateProgressBar();
    }

    @Override
    public void flush() throws IOException {
        outstream.flush();
    }

    @Override
    public void close() throws IOException {
        outstream.close();
    }

    public long getWrittenLength() {
        return bytesWritten;
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    private void updateProgressBar() {

        int progressPercent;

        if (contentSize > 0) { // Prevent division by zero and negative values
            progressPercent = (int) (100 * bytesWritten / contentSize);
            progressBar.setString(progressPercent + "% " + (bytesWritten / 1024) + "/" + (contentSize / 1024 + "kB"));
            progressBar.setValue(progressPercent);
        }

    }
}
