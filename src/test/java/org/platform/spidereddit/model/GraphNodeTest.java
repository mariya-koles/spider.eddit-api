package org.platform.spidereddit.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphNodeTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testNoArgsConstructor() {
        assertDoesNotThrow(() -> {
            GraphNode node = new GraphNode();
            assertNotNull(node);
            assertNull(node.getId());
        });
    }

    @Test
    void testAllArgsConstructor() {
        String testId = "test-node-id";
        
        assertDoesNotThrow(() -> {
            GraphNode node = new GraphNode(testId);
            assertNotNull(node);
            assertEquals(testId, node.getId());
        });
    }

    @Test
    void testSetterAndGetter() {
        GraphNode node = new GraphNode();
        String testId = "test-id";
        
        node.setId(testId);
        assertEquals(testId, node.getId());
    }

    @Test
    void testSetterWithNull() {
        GraphNode node = new GraphNode("initial-id");
        
        node.setId(null);
        assertNull(node.getId());
    }

    @Test
    void testSetterWithEmptyString() {
        GraphNode node = new GraphNode();
        
        node.setId("");
        assertEquals("", node.getId());
    }

    @Test
    void testEqualsAndHashCode() {
        GraphNode node1 = new GraphNode("test-id");
        GraphNode node2 = new GraphNode("test-id");
        GraphNode node3 = new GraphNode("different-id");
        GraphNode node4 = new GraphNode();
        GraphNode node5 = new GraphNode();

        // Test equals
        assertEquals(node1, node2);
        assertNotEquals(node1, node3);
        assertEquals(node4, node5); // Both have null ids
        assertNotEquals(node1, node4);

        // Test hashCode consistency
        assertEquals(node1.hashCode(), node2.hashCode());
        assertEquals(node4.hashCode(), node5.hashCode());
    }

    @Test
    void testToString() {
        GraphNode node = new GraphNode("test-id");
        String toString = node.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("test-id"));
        assertTrue(toString.contains("GraphNode"));
    }

    @Test
    void testToStringWithNull() {
        GraphNode node = new GraphNode();
        String toString = node.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("GraphNode"));
    }

    @Test
    void testJsonSerialization() throws Exception {
        GraphNode node = new GraphNode("test-node");
        
        String json = objectMapper.writeValueAsString(node);
        assertNotNull(json);
        assertTrue(json.contains("test-node"));
        assertTrue(json.contains("id"));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = "{\"id\":\"test-node\"}";
        
        GraphNode node = objectMapper.readValue(json, GraphNode.class);
        assertNotNull(node);
        assertEquals("test-node", node.getId());
    }

    @Test
    void testJsonSerializationWithNull() throws Exception {
        GraphNode node = new GraphNode();
        
        String json = objectMapper.writeValueAsString(node);
        assertNotNull(json);
        assertTrue(json.contains("null") || json.contains("id"));
    }

    @Test
    void testJsonDeserializationWithNull() throws Exception {
        String json = "{\"id\":null}";
        
        GraphNode node = objectMapper.readValue(json, GraphNode.class);
        assertNotNull(node);
        assertNull(node.getId());
    }

    @Test
    void testJsonDeserializationEmptyObject() throws Exception {
        String json = "{}";
        
        GraphNode node = objectMapper.readValue(json, GraphNode.class);
        assertNotNull(node);
        assertNull(node.getId());
    }

    @Test
    void testSpecialCharactersInId() {
        String specialId = "test-node_123!@#$%^&*()";
        GraphNode node = new GraphNode(specialId);
        
        assertEquals(specialId, node.getId());
    }

    @Test
    void testUnicodeCharactersInId() {
        String unicodeId = "test-node-🚀-测试-🎉";
        GraphNode node = new GraphNode(unicodeId);
        
        assertEquals(unicodeId, node.getId());
    }

    @Test
    void testLongId() {
        String longId = "a".repeat(1000);
        GraphNode node = new GraphNode(longId);
        
        assertEquals(longId, node.getId());
        assertEquals(1000, node.getId().length());
    }

    @Test
    void testImmutabilityOfId() {
        String originalId = "original-id";
        GraphNode node = new GraphNode(originalId);
        
        // Verify that modifying the original string doesn't affect the node
        String modifiedId = originalId + "-modified";
        assertEquals("original-id", node.getId());
        assertNotEquals(modifiedId, node.getId());
    }
} 