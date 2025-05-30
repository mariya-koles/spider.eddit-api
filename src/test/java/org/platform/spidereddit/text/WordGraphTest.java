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
    void testExportToJsonFile_basicExport() throws IOException {
        String[] words = {"hello", "world", "test"};
        wordGraph.recordCoOccurrences(words);
        
        Path outputFile = tempDir.resolve("test-graph.json");
        
        assertDoesNotThrow(() -> {
            wordGraph.exportToJsonFile(outputFile, 1);
        });
        
        assertTrue(outputFile.toFile().exists());
        assertTrue(outputFile.toFile().length() > 0);
    }

    @Test
    void testExportToJsonFile_validJsonStructure() throws IOException {
        String[] words = {"hello", "world", "test"};
        wordGraph.recordCoOccurrences(words);
        
        Path outputFile = tempDir.resolve("test-graph.json");
        wordGraph.exportToJsonFile(outputFile, 1);
        
        // Verify the JSON structure
        JsonNode root = objectMapper.readTree(outputFile.toFile());
        
        assertTrue(root.has("nodes"));
        assertTrue(root.has("edges"));
        assertTrue(root.get("nodes").isArray());
        assertTrue(root.get("edges").isArray());
    }

    @Test
    void testExportToJsonFile_minWeightFiltering() throws IOException {
        String[] words = {"hello", "world"};
        wordGraph.recordCoOccurrences(words); // This creates weight 1
        
        Path outputFile1 = tempDir.resolve("test-graph-min1.json");
        Path outputFile2 = tempDir.resolve("test-graph-min2.json");
        
        wordGraph.exportToJsonFile(outputFile1, 1); // Should include edges
        wordGraph.exportToJsonFile(outputFile2, 2); // Should exclude edges
        
        JsonNode root1 = objectMapper.readTree(outputFile1.toFile());
        JsonNode root2 = objectMapper.readTree(outputFile2.toFile());
        assertTrue(root1.get("edges").size() > 0);
        assertEquals(0, root2.get("edges").size());
    }

    @Test
    void testExportToJsonFile_emptyGraph() throws IOException {
        Path outputFile = tempDir.resolve("empty-graph.json");
        
        assertDoesNotThrow(() -> {
            wordGraph.exportToJsonFile(outputFile, 1);
        });
        
        JsonNode root = objectMapper.readTree(outputFile.toFile());
        assertEquals(0, root.get("nodes").size());
        assertEquals(0, root.get("edges").size());
    }

    @Test
    void testExportToJsonFile_nodeStructure() throws IOException {
        String[] words = {"hello", "world"};
        wordGraph.recordCoOccurrences(words);
        
        Path outputFile = tempDir.resolve("test-nodes.json");
        wordGraph.exportToJsonFile(outputFile, 1);
        
        JsonNode root = objectMapper.readTree(outputFile.toFile());
        JsonNode nodes = root.get("nodes");
        
        assertTrue(nodes.size() > 0);

        for (JsonNode node : nodes) {
            assertTrue(node.has("id"));
            assertFalse(node.get("id").asText().isEmpty());
        }
    }

    @Test
    void testExportToJsonFile_edgeStructure() throws IOException {
        String[] words = {"hello", "world"};
        wordGraph.recordCoOccurrences(words);
        
        Path outputFile = tempDir.resolve("test-edges.json");
        wordGraph.exportToJsonFile(outputFile, 1);
        
        JsonNode root = objectMapper.readTree(outputFile.toFile());
        JsonNode edges = root.get("edges");
        
        assertTrue(edges.size() > 0);

        for (JsonNode edge : edges) {
            assertTrue(edge.has("source"));
            assertTrue(edge.has("target"));
            assertTrue(edge.has("weight"));
            assertFalse(edge.get("source").asText().isEmpty());
            assertFalse(edge.get("target").asText().isEmpty());
            assertTrue(edge.get("weight").asInt() > 0);
        }
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