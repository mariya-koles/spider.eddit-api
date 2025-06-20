package org.platform.spidereddit.text;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.platform.spidereddit.model.GraphEdge;
import org.platform.spidereddit.model.GraphNode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WordGraph {

    // Nested structure: word1 -> (word2 -> count)
    private final Map<String, Map<String, AtomicInteger>> graph = new ConcurrentHashMap<>();

    public void recordCoOccurrences(String[] words) {
        int windowSize = 3;
        for (int i = 0; i < words.length; i++) {
            String w1 = words[i];
            for (int j = i + 1; j < words.length && j <= i + windowSize; j++) {
                String w2 = words[j];
                if (w1.equals(w2)) continue;

                // Order-insensitive edge
                String first = w1.compareTo(w2) < 0 ? w1 : w2;
                String second = w1.compareTo(w2) < 0 ? w2 : w1;

                graph
                        .computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                        .computeIfAbsent(second, k -> new AtomicInteger(0))
                        .incrementAndGet();
            }
        }
    }

    /**
     * Returns the word graph as a JSON string for a given min weight threshold
     */
    public String toJson(int minWeight) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(exportData(minWeight));
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize graph to JSON", e);
        }
    }

    /**
     * Returns the graph as a map of nodes and edges (for use in service/controller).
     */
    public Map<String, Object> exportData(int minWeight) {
        Set<GraphNode> nodes = new HashSet<>();
        List<GraphEdge> edges = new ArrayList<>();

        for (Map.Entry<String, Map<String, AtomicInteger>> entry : graph.entrySet()) {
            String source = entry.getKey();
            for (Map.Entry<String, AtomicInteger> inner : entry.getValue().entrySet()) {
                String target = inner.getKey();
                int weight = inner.getValue().get();

                if (weight >= minWeight) {
                    nodes.add(new GraphNode(source));
                    nodes.add(new GraphNode(target));
                    edges.add(new GraphEdge(source, target, weight));
                }
            }
        }

        return Map.of(
                "nodes", nodes,
                "edges", edges
        );
    }
}
