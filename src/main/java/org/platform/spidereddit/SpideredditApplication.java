package org.platform.spidereddit;

import org.platform.spidereddit.crawler.CrawlManager;
import org.platform.spidereddit.reddit.RedditAuthService;
import org.platform.spidereddit.reddit.RedditClient;
import org.platform.spidereddit.reddit.RedditConfig;
import org.platform.spidereddit.text.WordGraph;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

@SpringBootApplication
public class SpideredditApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(SpideredditApplication.class, args);

        WordGraph wordGraph = new WordGraph();

        RedditAuthService auth = new RedditAuthService(RedditConfig.CLIENT_ID, RedditConfig.CLIENT_SECRET, RedditConfig.USERNAME, RedditConfig.PASSWORD);
        RedditClient client = new RedditClient(auth.fetchAccessToken());

        String postId = client.extractPostId("");

        Set<String> usernames = client.getAllCommenters(postId, client.getAccessToken());

        CrawlManager manager = new CrawlManager(5, wordGraph, client.getAccessToken());
        manager.crawlUsers(usernames);

        wordGraph.exportToJsonFile(Paths.get("wordgraph.json"), 3); // only export edges with weight ≥ 3

    }

}
