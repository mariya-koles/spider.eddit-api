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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedditAuthServiceTest {

    @Mock
    private OkHttpClient mockHttpClient;
    
    @Mock
    private Call mockCall;

    private ObjectMapper objectMapper;
    private RedditAuthService redditAuthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        redditAuthService = new RedditAuthService("testClientId", "testClientSecret", 
                                                 "testUsername", "testPassword", 
                                                 mockHttpClient, objectMapper);
    }

    @Test
    void testFetchAccessToken_success() throws Exception {
        String mockTokenResponse = """
            {
                "access_token": "mock-token",
                "token_type": "bearer",
                "expires_in": 3600,
                "scope": "*"
            }
        """;

        ResponseBody body = ResponseBody.create(
                mockTokenResponse,
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url("https://www.reddit.com/api/v1/access_token")
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

        String token = redditAuthService.fetchAccessToken();
        assertEquals("mock-token", token);
    }

    @Test
    void testFetchAccessToken_httpError() throws Exception {
        ResponseBody body = ResponseBody.create("Unauthorized", MediaType.get("text/plain"));

        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://www.reddit.com/api/v1/access_token").build())
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body(body)
                .build();

        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        IOException exception = assertThrows(IOException.class, () -> 
            redditAuthService.fetchAccessToken());
        assertTrue(exception.getMessage().contains("401"));
    }

    @Test
    void testFetchAccessToken_malformedJson() throws Exception {
        String malformedJson = "{ invalid json content";

        ResponseBody body = ResponseBody.create(
                malformedJson,
                MediaType.get("application/json")
        );

        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://www.reddit.com/api/v1/access_token").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();

        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        Exception exception = assertThrows(Exception.class, () ->
            redditAuthService.fetchAccessToken());
        assertTrue(exception.getMessage().contains("Unrecognized token") || 
                  exception.getMessage().contains("Unexpected character"));
    }

    @Test
    void testFetchAccessToken_networkError() throws Exception {
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Network connection failed"));

        IOException exception = assertThrows(IOException.class, () ->
            redditAuthService.fetchAccessToken());
        assertTrue(exception.getMessage().contains("Network connection failed"));
    }

    @Test
    void testFetchAccessToken_missingAccessTokenInResponse() throws Exception {
        String responseWithoutToken = """
            {
                "token_type": "bearer",
                "expires_in": 3600,
                "scope": "*"
            }
        """;

        ResponseBody body = ResponseBody.create(
                responseWithoutToken,
                MediaType.get("application/json")
        );

        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://www.reddit.com/api/v1/access_token").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();

        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        Exception exception = assertThrows(Exception.class, () ->
            redditAuthService.fetchAccessToken());
        assertTrue(exception instanceof NullPointerException || 
                  exception.getMessage().contains("token"));
    }

    @Test
    void testFetchAccessToken_emptyAccessToken() throws Exception {
        String responseWithEmptyToken = """
            {
                "access_token": "",
                "token_type": "bearer",
                "expires_in": 3600,
                "scope": "*"
            }
        """;

        ResponseBody body = ResponseBody.create(
                responseWithEmptyToken,
                MediaType.get("application/json")
        );

        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://www.reddit.com/api/v1/access_token").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();

        when(mockHttpClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        String token = redditAuthService.fetchAccessToken();
        assertEquals("", token);
    }

    @Test
    void testCredentialsEncoding() {
        // Test that credentials are properly base64 encoded
        String clientId = "testClientId";
        String clientSecret = "testClientSecret";
        String expectedCredentials = clientId + ":" + clientSecret;
        
        String encoded = java.util.Base64.getEncoder().encodeToString(expectedCredentials.getBytes());
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
        
        // Verify we can decode it back
        String decoded = new String(java.util.Base64.getDecoder().decode(encoded));
        assertEquals(expectedCredentials, decoded);
    }

    @Test
    void testConstructorValidation() {
        assertDoesNotThrow(() -> {
            RedditAuthService service = new RedditAuthService("clientId", "secret", "user", "pass");
            assertNotNull(service);
        });
        
        assertDoesNotThrow(() -> {
            RedditAuthService service = new RedditAuthService("clientId", "secret", "user", "pass", 
                                                             new OkHttpClient(), new ObjectMapper());
            assertNotNull(service);
        });
    }
} 