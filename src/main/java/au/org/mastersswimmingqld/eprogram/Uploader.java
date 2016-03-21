package au.org.mastersswimmingqld.eprogram;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
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


public class Uploader {
	
	private static final Logger log = Logger.getLogger( Uploader.class.getName() );
    private Properties properties;
    //private CloseableHttpClient httpclient;
	
	private String filePath;
	private String username;
	private String password;
    private int meetId;

    String tempFilename;
    URI uri;
	
	/**
	 * @param filePath
	 * @param username
	 * @param password
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
            tempFilename = String.format("%s/%s.zip", tempdir, meetId);

            uri = URI.create(new StringBuilder("jar:file:").append(tempFilename).toString());

        } catch (IOException e) {
            log.severe(String.format("Unable to create temporary directory: %s", e.toString()));
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
		log.info(new StringBuffer("Set file path to ")
				.append(filePath).append(".").toString());
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
		log.info(new StringBuffer("Set username to ")
				.append(filePath).append(".").toString());
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
            StringBody bodyMeetId = new StringBody(new String().valueOf(meetId), ContentType.MULTIPART_FORM_DATA);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("userfile", bin)
                    .addPart("username", bodyUsername)
                    .addPart("password", bodyPassword)
                    .addPart("meetId", bodyMeetId)
                    .build();

            httppost.setEntity(reqEntity);

            log.info("executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);

            try {

                log.info("http response: " + response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    resEntity.getContent();
                    System.out.println("Response: " + IOUtils.toString(resEntity.getContent()));
                }
                EntityUtils.consume(resEntity);
            } catch (IOException e) {
                log.severe("Unable to upload");
                return false;
            } finally {
                response.close();
            }
        } catch (IOException e) {
            log.severe("Unable to connect");
            return false;
        }

		return true;
	}

}
