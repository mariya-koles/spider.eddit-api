package org.platform.spidereddit.reddit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RedditClient {

    private final Logger log = LoggerFactory.getLogger(UserHistoryFetcher.class);
    private static final String BASE_URL = "https://www.reddit.com/comments";
    private static final String USER_AGENT = "JavaSpideredditBot/1.0";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RedditClient() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String extractPostId(String url) {
        String[] parts = url.split("/");
        return IntStream.range(0, parts.length - 1)
                .filter(i -> parts[i].equals("comments"))
                .mapToObj(i -> parts[i + 1])
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Reddit post URL: " + url));
    }

    public Set<String> getTopLevelCommenters(String postId) throws IOException {
        String url = BASE_URL + "/" + postId + ".json";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody body = response.body();
            if (body == null) return Set.of();

            JsonNode root = objectMapper.readTree(body.string());

            JsonNode commentTree = root.get(1).get("data").get("children");

            Set<String> usernames = new HashSet<>();
            for (JsonNode commentNode : commentTree) {
                collectAuthors(commentNode, usernames);
            }
            return usernames;
        }
    }


    private void collectAuthors(JsonNode commentNode, Set<String> usernames) {
        Optional.ofNullable(commentNode.get("data"))
                .map(data -> data.get("author"))
                .map(JsonNode::asText)
                .filter(author -> !author.equals("[deleted"))
                .ifPresent(usernames::add);

        Optional.ofNullable(commentNode.get("data"))
                .map(d -> d.get("replies"))
                .map(r -> r.get("data"))
                .map(d -> d.get("children"))
                .filter(JsonNode::isArray)
                .ifPresent(children -> {
                    StreamSupport.stream(children.spliterator(), false)
                            .forEach(child -> collectAuthors(child, usernames));
                });
    }
}
