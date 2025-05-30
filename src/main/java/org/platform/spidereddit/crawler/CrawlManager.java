package org.platform.spidereddit.crawler;

import org.platform.spidereddit.reddit.UserHistoryFetcher;
import org.platform.spidereddit.text.WordGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CrawlManager {

    private final Logger log = LoggerFactory.getLogger(CrawlManager.class);
    private final ExecutorService executor;
    private final WordGraph wordGraph;
    private final String accessToken;


    public CrawlManager(int threadCount, WordGraph wordGraph, String accessToken) {
        this.executor =  Executors.newFixedThreadPool(threadCount);
        this.wordGraph = wordGraph;
        this.accessToken = accessToken;
    }

    public void crawlUsers(Set<String> usernames) {
        for (String username : usernames) {
            executor.submit(new UserHistoryFetcher(username, wordGraph, accessToken));
        }
        log.info("All tasks submitted. Awaiting completion...");
        shutdownAndWait();
    }

    public void shutdownAndWait() {
        executor.shutdown();
        log.info("Executor shutting down...");

        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.error("Timeout: Forcing shutdown...");
                executor.shutdownNow(); // cancel running tasks
            } else {
                log.info("All tasks completed — executor fully shut down.");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
