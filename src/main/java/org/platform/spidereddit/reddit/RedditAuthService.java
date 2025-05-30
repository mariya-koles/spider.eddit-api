package org.platform.spidereddit.reddit;
import lombok.AllArgsConstructor;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Base64;

@AllArgsConstructor
public class RedditAuthService {

    private static final String TOKEN_URL = "https://www.reddit.com/api/v1/access_token";

    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
