package org.platform.spidereddit.reddit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.platform.spidereddit.text.WordGraph;
import org.platform.spidereddit.utility.POSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

public class UserHistoryFetcher implements Runnable {

    private final Logger log = LoggerFactory.getLogger(UserHistoryFetcher.class);
    private final String username;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final CharArraySet STOP_WORDS = EnglishAnalyzer.getDefaultStopSet();
    private final WordGraph wordGraph;
    private final String accessToken;


    public UserHistoryFetcher(String username, WordGraph wordGraph, String accessToken) {
        this.username = username;
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.wordGraph = wordGraph;
        this.accessToken = accessToken;
    }

    @Override
    public void run() {
        try {
            String commentText = String.join(" ", fetchCommentWords());
            String postText = String.join(" ", fetchPostWords());

            POSFilter filter = new POSFilter(Set.of("NOUN", "ADJ", "VERB")); // nouns, verbs, adjectives

            List<String> commentWords = filter.filter(commentText);
            List<String> postWords = filter.filter(postText);

            List<String> words = new ArrayList<>();
            words.addAll(commentWords);
            words.addAll(postWords);


            Map<String, Integer> frequencyMap = new HashMap<>();
            for (String word : words) {
                frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
            }

            List<String> topWords = frequencyMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(500)
                    .map(Map.Entry::getKey)
                    .toList();

            if (!topWords.isEmpty()) {
                wordGraph.recordCoOccurrences(topWords.toArray(new String[0]));
            }
            log.info("Fetched {} words for user: {}", words.size(), username);

        } catch (IOException e) {
            log.error("Error fetching data for user {}: {}", username, e.getMessage());
        }
        log.info("Finished processing user: {}", username);
    }

    private List<String> fetchCommentWords() throws IOException {
        String url = "https://oauth.reddit.com/user/" + username + "/comments?limit=25";

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", RedditConfig.USER_AGENT)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == 429) {
                log.error("Rate limited while fetching user history. Retrying in 2 seconds...");
                Thread.sleep(2000);
                return fetchCommentWords();
            }

            if (!response.isSuccessful()) return List.of();

            JsonNode root = objectMapper.readTree(response.body().string());
            JsonNode children = root.path("data").path("children");

            return StreamSupport.stream(children.spliterator(), false)
                    .map(child -> child.path("data").path("body").asText(""))
                    .flatMap(body -> Arrays.stream(body.split("\\W+")))
                    .map(token -> token.toLowerCase(Locale.ENGLISH))
                    .filter(token -> !token.isBlank() && !STOP_WORDS.contains(token))
                    .toList();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> fetchPostWords() throws IOException {
        String url = "https://oauth.reddit.com/user/" + username + "/submitted?limit=25";

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", RedditConfig.USER_AGENT)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (response.code() == 429) {
                log.error("Rate limited while fetching user history. Retrying in 2 seconds...");
                Thread.sleep(2000);
                return fetchPostWords();
            }

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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
