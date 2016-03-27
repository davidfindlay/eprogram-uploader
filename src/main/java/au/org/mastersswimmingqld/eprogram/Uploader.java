package au.org.mastersswimmingqld.eprogram;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;


import java.io.File;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.swing.*;

import static java.lang.String.*;

public class Uploader {
	
	private static final Logger log = Logger.getLogger( Uploader.class.getName() );
    private Properties properties;
	
	private String filePath;
	private String username;
	private String password;
    private int meetId;
    private JProgressBar progressBar;

    public String getStatus() {
        return status;
    }

    private String status;

    String tempFilename;
    URI uri;
	
	/**
     * @param filePath path to the Meet Manager database
     * @param username MSQ SMS backend username
     * @param password MSQ SMS backend password
     */
	public Uploader(String filePath, String username, String password, int meetId) {

        // Load properties
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        } catch (Exception e) {
            log.severe("Unable to load properties.");
        }

		this.filePath = filePath;
		this.username = username;
		this.password = password;
        this.meetId = meetId;

        try {
            Path tempdir = Files.createTempDirectory("tempmm");
            tempdir.toFile().deleteOnExit();
            tempFilename = format("%s/%s.zip", tempdir, meetId);

            uri = URI.create("jar:file:" + tempFilename);

        } catch (IOException e) {
            log.severe(format("Unable to create temporary directory: %s", e.toString()));
        }
		
		log.info("Created uploader object for " + filePath);
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
        log.fine("Set file path to " + filePath + ".");
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    /**
     * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
        log.fine("Set username to " + filePath + ".");
    }

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
		log.info("Set password.");
	}

    /**
     * Create temporary Zip file
     */
    public boolean createTempZip() {
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, new HashMap<String, String>() {{ put("create", "true"); }})) {
            Path externalMMFile = Paths.get(filePath);
            Path pathInZipfile = zipfs.getPath(externalMMFile.getFileName().toString());

            // copy Meet Manager Database file into the zip file
            Files.copy(externalMMFile,pathInZipfile, StandardCopyOption.REPLACE_EXISTING );
        } catch (IOException e) {
            log.severe("Unable to create zip upload file!");
            return false;
        }

        log.info("Temporary zip file location: " + uri.toString());

        return true;
    }

    /**
	 * Upload function
	 */
	public boolean upload() {

        // Generate the temporary zip file
        if (!createTempZip()) {
            return false;
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            // Create HttpClient

            String url = properties.get("uploaderUrl").toString();
            HttpPost httppost = new HttpPost(url);

            FileBody bin = new FileBody(new File(tempFilename));
            StringBody bodyUsername = new StringBody(username, ContentType.MULTIPART_FORM_DATA);
            StringBody bodyPassword = new StringBody(password, ContentType.MULTIPART_FORM_DATA);
            StringBody bodyMeetId = new StringBody(valueOf(meetId), ContentType.MULTIPART_FORM_DATA);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("userfile", bin)
                    .addPart("username", bodyUsername)
                    .addPart("password", bodyPassword)
                    .addPart("meetId", bodyMeetId)
                    .build();

            long fileSize = reqEntity.getContentLength();
            httppost.setEntity(new CountingHttpEntity(reqEntity, progressBar));

            log.info("Uploading " + (fileSize / 1024) + "kB.");

            try (CloseableHttpResponse response = httpclient.execute(httppost)) {

                log.info("http response: " + response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    resEntity.getContent();
                    status = IOUtils.toString(resEntity.getContent());
                    log.info("Response: " + status);
                }
                EntityUtils.consume(resEntity);
            } catch (IOException e) {
                log.severe("Unable to upload");
                return false;
            }
        } catch (IOException e) {
            log.severe("Unable to connect");
            return false;
        }

		return true;
	}

}
