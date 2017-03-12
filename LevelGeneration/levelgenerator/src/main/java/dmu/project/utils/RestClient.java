package dmu.project.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by Dom on 20/02/2017.
 */

public class RestClient {
    private List<NameValuePair> params;
    private List<NameValuePair> headers;
    private String baseURL;
    public String response;
    public int responseCode;
    public String message;

    public enum RequestMethod {
        GET,
        POST
    }

    public RestClient(String baseURL) {
        this.baseURL = baseURL;
        headers = new ArrayList<>();
        params = new ArrayList<>();
    }

    public void addParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
    }

    public void addHeader(String name, String value) {
        headers.add(new BasicNameValuePair(name, value));
    }

    public void execute(RequestMethod method) throws IOException {
        switch (method) {
            case GET: {
                //add parameters
                HttpGet request = buildRequest(params);

                //add headers
                for (NameValuePair h : headers) {
                    request.addHeader(h.getName(), h.getValue());
                }

                executeRequest(request);
                break;
            }
            case POST: {
                HttpPost request = new HttpPost(baseURL);

                //add headers
                for (NameValuePair h : headers) {
                    request.addHeader(h.getName(), h.getValue());
                }

                if (!params.isEmpty()) {
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                }

                executeRequest(request);
                break;
            }
        }
    }

    public void executeRequest(HttpUriRequest request) throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpResponse httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                InputStream content = entity.getContent();
                response = IOUtils.toString(content);
                IOUtils.closeQuietly(content);
            }

        } catch (IOException e) {
            throw e;
        }
    }

    private HttpGet buildRequest(List<NameValuePair> params) throws UnsupportedEncodingException {
        String combinedParams = "";
        if (!params.isEmpty()) {
            combinedParams += "?";
            for (NameValuePair p : params) {
                String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                if (combinedParams.length() > 1) {
                    combinedParams += "&" + paramString;
                } else {
                    combinedParams += paramString;
                }
            }
        }
        return new HttpGet(baseURL + combinedParams);
    }

    public String getResponse() {
        return response;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getMessage() {
        return message;
    }
}
