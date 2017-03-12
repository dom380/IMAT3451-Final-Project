package com.mygdx.game.WeatherAPI;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import dmu.project.utils.RestClient;

/**
 * Created by Dom on 22/02/2017.
 */

public class WeatherClient {

    private String apiKey;
    private String apiUrl;
    private final ObjectMapper mapper;

    public WeatherClient(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public WeatherResponse getWeather(double latitude, double longitude) {
        String response = executeRequest(latitude, longitude);
        if(response == null){
            return null;
        }
        try {
            WeatherResponse weatherResponse;
            weatherResponse = mapper.readValue(response,WeatherResponse.class);
            return  weatherResponse;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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
            response = "{\"coord\":{\"lon\":-0.13,\"lat\":51.51},\"weather\":[{\"id\":300,\"main\":\"Drizzle\",\"description\":\"light intensity drizzle\",\"icon\":\"09d\"}],\"base\":\"stations\",\"main\":{\"temp\":12.25,\"pressure\":1008,\"humidity\":82,\"temp_min\":11,\"temp_max\":13},\"visibility\":10000,\"wind\":{\"speed\":7.2,\"deg\":250},\"clouds\":{\"all\":90},\"dt\":1487760600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.036,\"country\":\"GB\",\"sunrise\":1487746753,\"sunset\":1487784566},\"id\":2643743,\"name\":\"London\",\"cod\":200}";
        }
        return response;
    }

}
