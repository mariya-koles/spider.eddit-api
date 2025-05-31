package org.platform.spidereddit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.platform.spidereddit.crawler.CrawlManager;
import org.platform.spidereddit.reddit.RedditAuthService;
import org.platform.spidereddit.reddit.RedditClient;
import org.platform.spidereddit.reddit.RedditConfig;
import org.platform.spidereddit.text.WordGraph;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SpideredditService {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final WordGraph wordGraph = new WordGraph();
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedditAuthService auth = new RedditAuthService(RedditConfig.CLIENT_ID, RedditConfig.CLIENT_SECRET,
            RedditConfig.USERNAME, RedditConfig.PASSWORD);

    public Map<String, Object> crawlFromRedditUrl(String redditUrl) throws IOException {

        RedditClient client = new RedditClient(httpClient, objectMapper, auth.fetchAccessToken());
        String postId = client.extractPostId(redditUrl);
        Set<String> usernames = client.getAllCommenters(postId, client.getAccessToken());
        CrawlManager manager = new CrawlManager(5, wordGraph, client.getAccessToken());
        manager.crawlUsers(usernames);
        return wordGraph.exportData(3);
    }


}
