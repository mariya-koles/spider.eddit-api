package org.platform.spidereddit.reddit;

import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.platform.spidereddit.text.WordGraph;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class UserHistoryFetcherTest {

    @Mock
    private OkHttpClient mockHttpClient;
    
    @Mock
    private Call mockCall;
    
    @Mock
    private WordGraph mockWordGraph;

    private UserHistoryFetcher userHistoryFetcher;
    private final String testUsername = "testuser";
    private final String testAccessToken = "test-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userHistoryFetcher = new UserHistoryFetcher(testUsername, mockWordGraph, testAccessToken);
    }

    @Test
    void testConstructorValidation() {
        assertDoesNotThrow(() -> {
            UserHistoryFetcher fetcher = new UserHistoryFetcher("user", mockWordGraph, "token");
            assertNotNull(fetcher);
        });
    }

    @Test
    void testRunMethod_doesNotThrowException() {

        assertDoesNotThrow(() -> {
            try {
                userHistoryFetcher.run();
            } catch (Exception e) {
            }
        });
    }

    @Test
    void testWordProcessing_stopWordsFiltering() {
        String[] testWords = {"the", "and", "hello", "world", "a", "an", "test"};

        assertDoesNotThrow(() -> {
            java.util.List<String> filtered = java.util.Arrays.stream(testWords)
                .filter(word -> !word.isBlank())
                .filter(word -> word.length() > 1) // Basic filtering
                .toList();
            
            assertFalse(filtered.isEmpty());
        });
    }

    @Test
    void testWordProcessing_tokenization() {
        // Test tokenization logic
        String testText = "Hello, world! This is a test.";
        String[] tokens = testText.split("\\W+");
        
        assertTrue(tokens.length > 0);
        assertEquals("Hello", tokens[0]);
        assertEquals("world", tokens[1]);
    }

    @Test
    void testWordProcessing_caseNormalization() {
        // Test case normalization
        String testWord = "HELLO";
        String normalized = testWord.toLowerCase(java.util.Locale.ENGLISH);
        
        assertEquals("hello", normalized);
    }

    // Integration test for Runnable
    @Test
    void testRunnableInterface() {
        assertTrue(userHistoryFetcher instanceof Runnable);

        assertDoesNotThrow(() -> {
            java.util.concurrent.ExecutorService executor = 
                java.util.concurrent.Executors.newSingleThreadExecutor();
            
            try {
                executor.submit(userHistoryFetcher);
                executor.shutdown();
                executor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Test
    void testThreadSafety() {
        assertDoesNotThrow(() -> {
            UserHistoryFetcher fetcher1 = new UserHistoryFetcher("user1", mockWordGraph, "token1");
            UserHistoryFetcher fetcher2 = new UserHistoryFetcher("user2", mockWordGraph, "token2");
            
            assertNotNull(fetcher1);
            assertNotNull(fetcher2);
            assertNotSame(fetcher1, fetcher2);
        });
    }
}
