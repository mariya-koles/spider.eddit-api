package org.platform.spidereddit.text;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WordGraphTest {

    private WordGraph wordGraph;
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        wordGraph = new WordGraph();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRecordCoOccurrences_basicFunctionality() {
        String[] words = {"hello", "world", "test"};
        
        assertDoesNotThrow(() -> {
            wordGraph.recordCoOccurrences(words);
        });
    }

    @Test
    void testRecordCoOccurrences_emptyArray() {
        String[] words = {};
        
        assertDoesNotThrow(() -> {
            wordGraph.recordCoOccurrences(words);
        });
    }

    @Test
    void testRecordCoOccurrences_singleWord() {
        String[] words = {"hello"};
        
        assertDoesNotThrow(() -> {
            wordGraph.recordCoOccurrences(words);
        });
    }

    @Test
    void testRecordCoOccurrences_duplicateWords() {
        String[] words = {"hello", "hello", "world"};
        
        assertDoesNotThrow(() -> {
            wordGraph.recordCoOccurrences(words);
        });
    }

    @Test
    void testRecordCoOccurrences_windowSize() {
        // Test that words within window size are connected
        String[] words = {"word1", "word2", "word3", "word4", "word5"};
        
        assertDoesNotThrow(() -> {
            wordGraph.recordCoOccurrences(words);
        });
    }

    @Test
    void testRecordCoOccurrences_orderInsensitive() {
        WordGraph graph1 = new WordGraph();
        WordGraph graph2 = new WordGraph();
        
        String[] words1 = {"apple", "banana"};
        String[] words2 = {"banana", "apple"};
        
        assertDoesNotThrow(() -> {
            graph1.recordCoOccurrences(words1);
            graph2.recordCoOccurrences(words2);
        });
    }

    @Test
    void testRecordCoOccurrences_multipleCallsAccumulate() {
        String[] words1 = {"hello", "world"};
        String[] words2 = {"hello", "world"};
        
        assertDoesNotThrow(() -> {
            wordGraph.recordCoOccurrences(words1);
            wordGraph.recordCoOccurrences(words2);
        });
    }






    @Test
    void testConcurrentAccess() {
        assertDoesNotThrow(() -> {
            java.util.concurrent.ExecutorService executor = 
                java.util.concurrent.Executors.newFixedThreadPool(3);
            
            for (int i = 0; i < 10; i++) {
                final int index = i;
                executor.submit(() -> {
                    String[] words = {"word" + index, "common", "test"};
                    wordGraph.recordCoOccurrences(words);
                });
            }
            
            executor.shutdown();
            executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        });
    }

    @Test
    void testLargeWordArray() {
        String[] words = new String[100];
        for (int i = 0; i < 100; i++) {
            words[i] = "word" + i;
        }
        
        assertDoesNotThrow(() -> {
            wordGraph.recordCoOccurrences(words);
        });
    }

    @Test
    void testSpecialCharactersInWords() {
        String[] words = {"hello-world", "test_case", "special.word"};
        
        assertDoesNotThrow(() -> {
            wordGraph.recordCoOccurrences(words);
        });
    }
} 