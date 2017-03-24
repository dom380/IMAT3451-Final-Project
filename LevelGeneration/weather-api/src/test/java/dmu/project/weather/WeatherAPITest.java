package dmu.project.weather;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.URI;

import cz.msebera.android.httpclient.client.methods.HttpUriRequest;

/**
 * Created by Dom on 12/03/2017.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(WeatherClient.class)
public class WeatherAPITest {

    private double[] defaultLatLong = {52.629124, -1.139688};
    private String apiURL = "http://api.openweathermap.org/data/2.5/weather";
    private String apiKey = "2c2e5d04d5f1c71108c7d2e4719a04fb";
    private String expectedURI = "http://api.openweathermap.org/data/2.5/weather?lat=52.629124&lon=-1.139688&units=metric&appid=2c2e5d04d5f1c71108c7d2e4719a04fb";
    private String expectedResponse = "{\"coord\":{\"lon\":-1.14,\"lat\":52.63},\"weather\":[{\"id\":801,\"main\":\"Clouds\",\"description\":\"few clouds\",\"icon\":\"02d\"}],\"base\":\"stations\",\"main\":{\"temp\":12.34,\"pressure\":1013,\"humidity\":81,\"temp_min\":12,\"temp_max\":13},\"visibility\":10000,\"wind\":{\"speed\":3.1,\"deg\":260},\"clouds\":{\"all\":20},\"dt\":1489319400,\"sys\":{\"type\":1,\"id\":5106,\"message\":0.0026,\"country\":\"GB\",\"sunrise\":1489299868,\"sunset\":1489341881},\"id\":3333165,\"name\":\"City of Leicester\",\"cod\":200}";
    private String invalidResponse = "this is not valid json";

    @Test
    public void buildRESTRequestTest() throws Exception {
        WeatherClient client = new WeatherClient(apiURL, apiKey);
        RestClient restClient = PowerMockito.spy( new RestClient(apiURL));
        final HttpUriRequest[] request = new HttpUriRequest[1];
        PowerMockito.doAnswer(
                new Answer<HttpUriRequest>() {
                    @Override
                    public HttpUriRequest answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        request[0] = (HttpUriRequest) args[0];
                        return (HttpUriRequest) args[0];
                    }
                }
        ).when(restClient).executeRequest(Mockito.any(HttpUriRequest.class)); //Redirect the HTTP request object to the test when passed to the RestClient.
        PowerMockito.doCallRealMethod().when(restClient).addParam(Mockito.anyString(),Mockito.anyString());
        PowerMockito.doCallRealMethod().when(restClient).execute(RestClient.RequestMethod.GET); //Otherwise execute normal behaviour.
        PowerMockito.whenNew(RestClient.class).withArguments(apiURL).thenReturn(restClient);
        client.getWeather(defaultLatLong[0], defaultLatLong[1]);
        Assert.assertNotNull("HTTP Request shouldn't be null", request[0]);
        URI uri = request[0].getURI();
        Assert.assertNotNull("URI shouldn't be null",uri);
        Assert.assertEquals("URI should match", expectedURI, uri.toString());
    }

    @Test
    public void GetWeatherAndDeserialiseResponseTest() throws Exception {
        RestClient restClient = PowerMockito.mock(RestClient.class);
        PowerMockito.doNothing().when(restClient).execute(RestClient.RequestMethod.GET);
        PowerMockito.doReturn(expectedResponse).when(restClient).getResponse();
        PowerMockito.whenNew(RestClient.class).withArguments(apiURL).thenReturn(restClient);

        WeatherClient client = new WeatherClient(apiURL, apiKey);
        WeatherResponse weather = client.getWeather(defaultLatLong[0], defaultLatLong[1]);
        Assert.assertNotNull("Response should not be null", weather);
        Assert.assertEquals("Temp should match", 12.34, weather.getMain().getTemp(), 0.001);
        Assert.assertEquals("Main should match", "Clouds", weather.getWeather().get(0).getMain());
        Assert.assertEquals("ConditionCode should match", WeatherResponse.ConditionCode.FEW_CLOUDS, weather.getWeather().get(0).getId());
        Assert.assertEquals("Description should match", "few clouds", weather.getWeather().get(0).getDescription());
    }

    @Test
    public void GetWeatherGetWeatherAndDeserialiseResponse_invalid() throws Exception {
        RestClient restClient = PowerMockito.mock(RestClient.class);
        PowerMockito.doNothing().when(restClient).execute(RestClient.RequestMethod.GET);
        PowerMockito.doReturn(invalidResponse).when(restClient).getResponse();
        PowerMockito.whenNew(RestClient.class).withArguments(apiURL).thenReturn(restClient);

        WeatherClient client = new WeatherClient(apiURL, apiKey);
        WeatherResponse weather = client.getWeather(defaultLatLong[0], defaultLatLong[1]);
        Assert.assertNull("The response should be null and no uncaught exception should be thrown.",weather);
    }
}
