package au.org.mastersswimmingqld.eprogram;

import java.lang.reflect.Type;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Created by david on 17/03/2016.
 */
public class MeetList {

    private static final Logger log = Logger.getLogger( MeetList.class.getName() );
    private ArrayList<Meet> meets;
    private Properties properties;
    private String jsonData;

    public MeetList() {

        // Load properties
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        } catch (Exception e) {
            log.severe("Unable to load properties.");
        }

        getJSONData();
        parseJSONData();

    }

    /**
     * Requests the list of available meets from Swimman
     */
    private void getJSONData() {

        try (CloseableHttpClient httpClient = HttpClients.createDefault();) {

            HttpGet getRequest = new HttpGet(properties.getProperty("meetListURl"));
            getRequest.addHeader("accept", "application/json");

            HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;

            while ((output = br.readLine()) != null) {
                jsonData = output;
                log.info("Retrieved list of meets");
            }

        } catch (ClientProtocolException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    /**
     * Parses the JSON list and populates the meet arraylist
     */
    private void parseJSONData() {
        //System.out.println(jsonData);

        Type type = new TypeToken<ArrayList<Meet>>() {}.getType();
        meets = new Gson().fromJson(jsonData, type);

    }

    public String[] getList() {
        String[] meetList = new String[meets.size()];

        for (int i = 0; i < meets.size(); i++) {
            meetList[i] = meets.get(i).getName();
        }

        return meetList;
    }

    /**
     * Finds a meet by meet name
     */
    public int findMeetByName(String name) {

        if (name != null) {
            for (Meet meet : meets) {
                if (meet.getName() == name) {
                    return meet.getId();
                }
            }

        }

        return -1;
    }
}
