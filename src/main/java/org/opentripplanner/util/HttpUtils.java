package org.opentripplanner.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class HttpUtils {

    private static final long TIMEOUT_CONNECTION = 5000;
    private static final int TIMEOUT_SOCKET = 5000;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static InputStream getData(String url) throws IOException {
        return getData(url, null, null);
    }

    public static InputStream getData(String url, String requestHeaderName, String requestHeaderValue) throws IOException {
        HttpGet httpget = new HttpGet(url);
        if (requestHeaderValue != null) {
            httpget.addHeader(requestHeaderName, requestHeaderValue);
        }
        HttpClient httpclient = getClient();
        HttpResponse response = httpclient.execute(httpget);
        if (response.getStatusLine().getStatusCode() != 200)
            return null;

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        return entity.getContent();
    }

    public static <T> T postData(String url, String data, TypeReference<T> type) {
        try {
            HttpPost request = new HttpPost(url);
            request.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
            HttpClient client = getClient();
            request.addHeader("content-type", "application/json");
            request.addHeader("accept", "application/json");
            HttpResponse response = client.execute(request);
            String json = EntityUtils.toString(response.getEntity(), "UTF-8");
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T getData(URI uri, TypeReference<T> type, int timeout) {
        try {
            HttpGet request = new HttpGet(uri);
            HttpClient client = getClient(timeout, timeout);
            request.addHeader("content-type", "application/json");
            request.addHeader("accept", "application/json");
            HttpResponse response = client.execute(request);
            String json = EntityUtils.toString(response.getEntity(), "UTF-8");
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void testUrl(String url) throws IOException {
        HttpHead head = new HttpHead(url);
        HttpClient httpclient = getClient();
        HttpResponse response = httpclient.execute(head);

        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() == 404) {
            throw new FileNotFoundException();
        }

        if (status.getStatusCode() != 200) {
            throw new RuntimeException("Could not get URL: " + status.getStatusCode() + ": "
                    + status.getReasonPhrase());
        }
    }

    public static <T>  T postDataWithPassword(String url, String data, TypeReference<T> type, String password) {
                try {
                       HttpPost request = new HttpPost(url);
                       request.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
                        HttpClient client = getClient();
                        request.addHeader("content-type", "application/json");
                        request.addHeader("Authorization", "Bearer " + password);
                        request.addHeader("accept", "application/json");
                        HttpResponse response = client.execute(request);
                        String json = EntityUtils.toString(response.getEntity(), "UTF-8");
                        return objectMapper.readValue(json, type);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
              return null;
           }

    private static HttpClient getClient() {
        return getClient(TIMEOUT_SOCKET, TIMEOUT_CONNECTION);
    }

    private static HttpClient getClient(int socket_timeout, long connection_timeout) {
        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(socket_timeout).build())
                .setConnectionTimeToLive(connection_timeout, TimeUnit.MILLISECONDS)
                .build();

        return httpClient;
    }

}
