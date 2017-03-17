package dmu.project.weather;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by Dom on 22/02/2017.
 * Client to access OpenWeatherMap API.
 */

public class WeatherClient {

    private String apiKey;
    private String apiUrl;
    private final ObjectMapper mapper;

    /**
     * Constructor
     *
     * @param apiUrl The base URL to the API.
     * @param apiKey The client's API key.
     */
    public WeatherClient(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Query the OpenWeatherMap service for the current weather at the specified location.
     *
     * @param latitude The latitude to look up.
     * @param longitude The longitude to look up.
     * @return The current weather at that location or null if the query failed.
     */
    public WeatherResponse getWeather(double latitude, double longitude) {
        String response = executeRequest(latitude, longitude);
        if(response == null){
            return null;
        }
        try {
            WeatherResponse weatherResponse;
            weatherResponse = mapper.readValue(response, WeatherResponse.class);
            return  weatherResponse;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    /**
     * Builds and executes the REST request.
     *
     * @param latitude The latitude to look up.
     * @param longitude The longitude to look up.
     * @return The JSON response from the server.
     */
    private String executeRequest(double latitude, double longitude) {
        String response = null;
        try {
            RestClient client = new RestClient(apiUrl);
            client.addParam("lat", String.valueOf(latitude));
            client.addParam("lon", String.valueOf(longitude));
            client.addParam("units", "metric");
            client.addParam("appid", apiKey);

            client.execute(RestClient.RequestMethod.GET);
            response = client.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

}