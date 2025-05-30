package org.platform.spidereddit.reddit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Locale;
import java.util.stream.StreamSupport;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

public class UserHistoryFetcher implements Runnable {

    private final Logger log = LoggerFactory.getLogger(UserHistoryFetcher.class);
    private final String username;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final CharArraySet STOP_WORDS = EnglishAnalyzer.getDefaultStopSet();

    public UserHistoryFetcher(String username) {
        this.username = username;
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try {
            List<String> words = new ArrayList<>();
            words.addAll(fetchCommentWords());
            words.addAll(fetchPostWords());

            // TODO: Pass words to a central co-occurrence analyzer
            log.info("Fetched {} words for user: {}", words.size(), username);

        } catch (IOException e) {
            log.error("Error fetching data for user {}: {}", username, e.getMessage());
        }
    }

    private List<String> fetchCommentWords() throws IOException {
        String url = "https://www.reddit.com/user/" + username + "/comments.json?limit=25";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "JavaSpideredditBot/1.0")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) return List.of();

            JsonNode root = objectMapper.readTree(response.body().string());
            JsonNode children = root.path("data").path("children");

            return StreamSupport.stream(children.spliterator(), false)
                    .map(child -> child.path("data").path("body").asText(""))
                    .flatMap(body -> Arrays.stream(body.split("\\W+")))
                    .map(token -> token.toLowerCase(Locale.ENGLISH))
                    .filter(token -> !token.isBlank() && !STOP_WORDS.contains(token))
                    .toList();
        }
    }

    private List<String> fetchPostWords() throws IOException {
        String url = "https://www.reddit.com/user/" + username + "/submitted.json?limit=25";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "JavaSpideredditBot/1.0")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) return List.of();

            JsonNode root = objectMapper.readTree(response.body().string());
            JsonNode children = root.path("data").path("children");

            return StreamSupport.stream(children.spliterator(), false)
                    .flatMap(child -> Arrays.stream(new String[] {
                            child.path("data").path("title").asText(""),
                            child.path("data").path("selftext").asText("")
                    }))
                    .flatMap(text -> Arrays.stream(text.split("\\W+")))
                    .map(token -> token.toLowerCase(Locale.ENGLISH))
                    .filter(token -> !token.isBlank() && !STOP_WORDS.contains(token))
                    .toList();
        }
    }
}
