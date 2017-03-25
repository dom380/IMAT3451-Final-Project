package dmu.project.weather;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by Dom on 22/02/2017.
 * Client to access OpenWeatherMap API.
 */

public class WeatherClient {

    private String mApiKey;
    private String mApiUrl;
    private final ObjectMapper mMapper;

    /**
     * Constructor
     *
     * @param mApiUrl The base URL to the API.
     * @param mApiKey The client's API key.
     */
    public WeatherClient(String mApiUrl, String mApiKey) {
        this.mApiUrl = mApiUrl;
        this.mApiKey = mApiKey;
        mMapper = new ObjectMapper();
        mMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Query the OpenWeatherMap service for the current weather at the specified location.
     *
     * @param latitude  The latitude to look up.
     * @param longitude The longitude to look up.
     * @return The current weather at that location or null if the query failed.
     */
    public WeatherResponse getWeather(double latitude, double longitude) {
        String response = null;
        try {
            response = executeRequest(latitude, longitude);
            if (response == null) {
                return null;
            }
            WeatherResponse weatherResponse;
            weatherResponse = mMapper.readValue(response, WeatherResponse.class);
            return weatherResponse;
        } catch (IOException e) {
            e.printStackTrace(); //Log error to console and return null.
            return null;
        }
    }
    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    /**
     * Builds and executes the REST request.
     *
     * @param latitude  The latitude to look up.
     * @param longitude The longitude to look up.
     * @return The JSON response from the server.
     */
    private String executeRequest(double latitude, double longitude) throws IOException {
        String response = null;
        RestClient client = new RestClient(mApiUrl);
        client.addParam("lat", String.valueOf(latitude));
        client.addParam("lon", String.valueOf(longitude));
        client.addParam("units", "metric");
        client.addParam("appid", mApiKey);
        client.execute(RestClient.RequestMethod.GET);
        response = client.getResponse();
        return response;
    }

}