package org.platform.spidereddit.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphEdgeTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testNoArgsConstructor() {
        assertDoesNotThrow(() -> {
            GraphEdge edge = new GraphEdge();
            assertNotNull(edge);
            assertNull(edge.getSource());
            assertNull(edge.getTarget());
            assertEquals(0, edge.getWeight());
        });
    }

    @Test
    void testAllArgsConstructor() {
        String source = "node1";
        String target = "node2";
        int weight = 5;
        
        assertDoesNotThrow(() -> {
            GraphEdge edge = new GraphEdge(source, target, weight);
            assertNotNull(edge);
            assertEquals(source, edge.getSource());
            assertEquals(target, edge.getTarget());
            assertEquals(weight, edge.getWeight());
        });
    }

    @Test
    void testSettersAndGetters() {
        GraphEdge edge = new GraphEdge();
        String source = "source-node";
        String target = "target-node";
        int weight = 10;
        
        edge.setSource(source);
        edge.setTarget(target);
        edge.setWeight(weight);
        
        assertEquals(source, edge.getSource());
        assertEquals(target, edge.getTarget());
        assertEquals(weight, edge.getWeight());
    }

    @Test
    void testSettersWithNull() {
        GraphEdge edge = new GraphEdge("initial-source", "initial-target", 5);
        
        edge.setSource(null);
        edge.setTarget(null);
        edge.setWeight(0);
        
        assertNull(edge.getSource());
        assertNull(edge.getTarget());
        assertEquals(0, edge.getWeight());
    }

    @Test
    void testSettersWithEmptyStrings() {
        GraphEdge edge = new GraphEdge();
        
        edge.setSource("");
        edge.setTarget("");
        
        assertEquals("", edge.getSource());
        assertEquals("", edge.getTarget());
    }

    @Test
    void testNegativeWeight() {
        GraphEdge edge = new GraphEdge("source", "target", -5);
        
        assertEquals(-5, edge.getWeight());
        
        edge.setWeight(-10);
        assertEquals(-10, edge.getWeight());
    }

    @Test
    void testZeroWeight() {
        GraphEdge edge = new GraphEdge("source", "target", 0);
        
        assertEquals(0, edge.getWeight());
    }

    @Test
    void testLargeWeight() {
        int largeWeight = Integer.MAX_VALUE;
        GraphEdge edge = new GraphEdge("source", "target", largeWeight);
        
        assertEquals(largeWeight, edge.getWeight());
    }

    @Test
    void testEqualsAndHashCode() {
        GraphEdge edge1 = new GraphEdge("source", "target", 5);
        GraphEdge edge2 = new GraphEdge("source", "target", 5);
        GraphEdge edge3 = new GraphEdge("different", "target", 5);
        GraphEdge edge4 = new GraphEdge("source", "different", 5);
        GraphEdge edge5 = new GraphEdge("source", "target", 10);
        GraphEdge edge6 = new GraphEdge();
        GraphEdge edge7 = new GraphEdge();

        // Test equals
        assertEquals(edge1, edge2);
        assertNotEquals(edge1, edge3);
        assertNotEquals(edge1, edge4);
        assertNotEquals(edge1, edge5);
        assertEquals(edge6, edge7); // Both have null values

        // Test hashCode consistency
        assertEquals(edge1.hashCode(), edge2.hashCode());
        assertEquals(edge6.hashCode(), edge7.hashCode());
    }

    @Test
    void testToString() {
        GraphEdge edge = new GraphEdge("source", "target", 5);
        String toString = edge.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("source"));
        assertTrue(toString.contains("target"));
        assertTrue(toString.contains("5"));
        assertTrue(toString.contains("GraphEdge"));
    }

    @Test
    void testToStringWithNulls() {
        GraphEdge edge = new GraphEdge();
        String toString = edge.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("GraphEdge"));
    }

    @Test
    void testJsonSerialization() throws Exception {
        GraphEdge edge = new GraphEdge("node1", "node2", 3);
        
        String json = objectMapper.writeValueAsString(edge);
        assertNotNull(json);
        assertTrue(json.contains("node1"));
        assertTrue(json.contains("node2"));
        assertTrue(json.contains("3"));
        assertTrue(json.contains("source"));
        assertTrue(json.contains("target"));
        assertTrue(json.contains("weight"));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = "{\"source\":\"node1\",\"target\":\"node2\",\"weight\":3}";
        
        GraphEdge edge = objectMapper.readValue(json, GraphEdge.class);
        assertNotNull(edge);
        assertEquals("node1", edge.getSource());
        assertEquals("node2", edge.getTarget());
        assertEquals(3, edge.getWeight());
    }

    @Test
    void testJsonSerializationWithNulls() throws Exception {
        GraphEdge edge = new GraphEdge();
        
        String json = objectMapper.writeValueAsString(edge);
        assertNotNull(json);
        assertTrue(json.contains("null") || json.contains("source"));
        assertTrue(json.contains("0") || json.contains("weight"));
    }

    @Test
    void testJsonDeserializationWithNulls() throws Exception {
        String json = "{\"source\":null,\"target\":null,\"weight\":0}";
        
        GraphEdge edge = objectMapper.readValue(json, GraphEdge.class);
        assertNotNull(edge);
        assertNull(edge.getSource());
        assertNull(edge.getTarget());
        assertEquals(0, edge.getWeight());
    }

    @Test
    void testJsonDeserializationEmptyObject() throws Exception {
        String json = "{}";
        
        GraphEdge edge = objectMapper.readValue(json, GraphEdge.class);
        assertNotNull(edge);
        assertNull(edge.getSource());
        assertNull(edge.getTarget());
        assertEquals(0, edge.getWeight());
    }

    @Test
    void testSpecialCharactersInNodes() {
        String specialSource = "source_123!@#$%^&*()";
        String specialTarget = "target-🚀-测试-🎉";
        GraphEdge edge = new GraphEdge(specialSource, specialTarget, 1);
        
        assertEquals(specialSource, edge.getSource());
        assertEquals(specialTarget, edge.getTarget());
    }

    @Test
    void testSameSourceAndTarget() {
        String node = "same-node";
        GraphEdge edge = new GraphEdge(node, node, 1);
        
        assertEquals(node, edge.getSource());
        assertEquals(node, edge.getTarget());
        assertEquals(edge.getSource(), edge.getTarget());
    }

    @Test
    void testLongNodeNames() {
        String longSource = "a".repeat(1000);
        String longTarget = "b".repeat(1000);
        GraphEdge edge = new GraphEdge(longSource, longTarget, 1);
        
        assertEquals(longSource, edge.getSource());
        assertEquals(longTarget, edge.getTarget());
        assertEquals(1000, edge.getSource().length());
        assertEquals(1000, edge.getTarget().length());
    }

    @Test
    void testWeightBoundaries() {
        GraphEdge edge = new GraphEdge("source", "target", 0);
        
        // Test minimum int value
        edge.setWeight(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, edge.getWeight());
        
        // Test maximum int value
        edge.setWeight(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, edge.getWeight());
    }

    @Test
    void testImmutabilityOfStrings() {
        String originalSource = "original-source";
        String originalTarget = "original-target";
        GraphEdge edge = new GraphEdge(originalSource, originalTarget, 1);

        String modifiedSource = originalSource + "-modified";
        String modifiedTarget = originalTarget + "-modified";
        
        assertEquals("original-source", edge.getSource());
        assertEquals("original-target", edge.getTarget());
        assertNotEquals(modifiedSource, edge.getSource());
        assertNotEquals(modifiedTarget, edge.getTarget());
    }
} 