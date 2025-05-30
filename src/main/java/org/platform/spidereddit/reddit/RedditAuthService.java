package org.platform.spidereddit.reddit;
import lombok.AllArgsConstructor;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Base64;

public class RedditAuthService {

    private static final String TOKEN_URL = "https://www.reddit.com/api/v1/access_token";

    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Constructor for production use
    public RedditAuthService(String clientId, String clientSecret, String username, String password) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // Constructor for testing with dependency injection
    public RedditAuthService(String clientId, String clientSecret, String username, String password, 
                           OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public String fetchAccessToken() throws IOException {
        String credentials = clientId + ":" + clientSecret;
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("username", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .header("Authorization", basicAuth)
                .header("User-Agent", RedditConfig.USER_AGENT)
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OAuth token request failed: " + response.code());
            }

            JsonNode json = objectMapper.readTree(response.body().string());
            return json.get("access_token").asText();
        }
    }
}
