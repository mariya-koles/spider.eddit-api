package org.platform.spidereddit.reddit;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedditClientTest {

    @Mock private OkHttpClient mockHttpClient;
    private ObjectMapper objectMapper;

    @Mock
    Call mockCall;

    RedditClient redditClient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        MockitoAnnotations.openMocks(this);
        redditClient = new RedditClient(mockHttpClient, objectMapper, "mock-token");
    }

    @Test
    void testGetAllCommenters_success() throws Exception {
        String fakeJson = """
            [ {}, {
                "data": {
                    "children": [
                        { "data": { "author": "testuser" } }
                    ]
                }
            }]
        """;

        ResponseBody body = ResponseBody.create(
                fakeJson,
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url("https://oauth.reddit.com/comments/abc123.json")
                .build();

        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();

        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        Set<String> result = redditClient.getAllCommenters("abc123", "mock-token");

        assertTrue(result.contains("testuser"));
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllCommenters_emptyResponse_shouldReturnEmptySet() throws Exception {
        String emptyJson = """
        [ {}, {
            "data": {
                "children": []
            }
        }]
    """;

        ResponseBody body = ResponseBody.create(
                emptyJson,
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url("https://oauth.reddit.com/comments/xyz789.json")
                .build();

        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();

        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        Set<String> result = redditClient.getAllCommenters("xyz789", "mock-token");

        assertTrue(result.isEmpty(), "Expected empty result when no commenters are present");
    }

    @Test
    void testGetAllCommenters_malformedJson_shouldThrowException() throws Exception {
        String badJson = "not a valid json";

        ResponseBody body = ResponseBody.create(
                badJson,
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url("https://oauth.reddit.com/comments/broken.json")
                .build();

        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();

        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        Exception exception = assertThrows(Exception.class, () ->
                redditClient.getAllCommenters("broken", "mock-token")
        );

        String message = exception.getMessage();
        assertTrue(message.contains("Unrecognized token") || message.contains("Unexpected character"),
                "Expected exception about malformed JSON");
    }

    @Test
    void testGetAllCommenters_http403_shouldThrowException() throws Exception {
        ResponseBody body = ResponseBody.create("Forbidden", MediaType.get("text/plain"));

        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://oauth.reddit.com/comments/abc123.json").build())
                .protocol(Protocol.HTTP_1_1)
                .code(403)
                .message("Forbidden")
                .body(body)
                .build();

        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        Exception exception = assertThrows(Exception.class, () ->
                redditClient.getAllCommenters("abc123", "mock-token")
        );

        assertTrue(exception.getMessage().contains("403"));
    }

    @Test
    void testGetAllCommenters_networkFailure_shouldThrowException() throws Exception {
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));

        Exception exception = assertThrows(IOException.class, () ->
                redditClient.getAllCommenters("abc123", "mock-token")
        );

        assertTrue(exception.getMessage().contains("Network error"));
    }

    @Test
    void testGetAllCommenters_duplicateAuthors_shouldReturnUniqueSet() throws Exception {
        String json = """
        [ {}, {
            "data": {
                "children": [
                    { "data": { "author": "user1" } },
                    { "data": { "author": "user1" } },
                    { "data": { "author": "user2" } }
                ]
            }
        }]
    """;

        ResponseBody body = ResponseBody.create(json, MediaType.get("application/json"));
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://oauth.reddit.com/comments/abc123.json").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();

        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        Set<String> result = redditClient.getAllCommenters("abc123", "mock-token");

        assertEquals(2, result.size());
        assertTrue(result.contains("user1"));
        assertTrue(result.contains("user2"));
    }

    @Test
    void testGetAllCommenters_missingAuthor_shouldSkipEntry() throws Exception {
        String json = """
        [ {}, {
            "data": {
                "children": [
                    { "data": {} },
                    { "data": { "author": null } },
                    { "data": { "author": "validUser" } }
                ]
            }
        }]
    """;

        ResponseBody body = ResponseBody.create(json, MediaType.get("application/json"));
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://oauth.reddit.com/comments/abc123.json").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();

        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        Set<String> result = redditClient.getAllCommenters("abc123", "mock-token");

        assertEquals(1, result.size());
        assertTrue(result.contains("validuser"));
    }





}
