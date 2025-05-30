package org.platform.spidereddit.crawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.platform.spidereddit.text.WordGraph;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrawlManagerTest {

    @Mock
    private WordGraph mockWordGraph;

    private CrawlManager crawlManager;
    private final String testAccessToken = "test-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConstructorValidation() {
        assertDoesNotThrow(() -> {
            CrawlManager manager = new CrawlManager(2, mockWordGraph, testAccessToken);
            assertNotNull(manager);
        });
    }

    @Test
    void testConstructorWithDifferentThreadCounts() {
        assertDoesNotThrow(() -> {
            CrawlManager manager1 = new CrawlManager(1, mockWordGraph, testAccessToken);
            CrawlManager manager2 = new CrawlManager(5, mockWordGraph, testAccessToken);
            CrawlManager manager3 = new CrawlManager(10, mockWordGraph, testAccessToken);
            
            assertNotNull(manager1);
            assertNotNull(manager2);
            assertNotNull(manager3);
        });
    }

    @Test
    void testCrawlUsers_emptySet() {
        crawlManager = new CrawlManager(2, mockWordGraph, testAccessToken);
        Set<String> emptyUsernames = new HashSet<>();
        
        assertDoesNotThrow(() -> {
            crawlManager.crawlUsers(emptyUsernames);
        });
    }

    @Test
    void testCrawlUsers_singleUser() {
        crawlManager = new CrawlManager(2, mockWordGraph, testAccessToken);
        Set<String> usernames = Set.of("testuser");
        
        assertDoesNotThrow(() -> {
            crawlManager.crawlUsers(usernames);
        });
    }

    @Test
    void testCrawlUsers_multipleUsers() {
        crawlManager = new CrawlManager(3, mockWordGraph, testAccessToken);
        Set<String> usernames = Set.of("user1", "user2", "user3", "user4", "user5");
        
        assertDoesNotThrow(() -> {
            crawlManager.crawlUsers(usernames);
        });
    }

    @Test
    void testShutdownAndWait_immediateShutdown() {
        crawlManager = new CrawlManager(2, mockWordGraph, testAccessToken);
        
        assertDoesNotThrow(() -> {
            crawlManager.shutdownAndWait();
        });
    }

    @Test
    void testShutdownAndWait_afterSubmittingTasks() {
        crawlManager = new CrawlManager(2, mockWordGraph, testAccessToken);
        Set<String> usernames = Set.of("user1", "user2");
        
        assertDoesNotThrow(() -> {
            crawlManager.crawlUsers(usernames);
            // crawlUsers already calls shutdownAndWait internally
        });
    }

    @Test
    void testMultipleShutdownCalls() {
        crawlManager = new CrawlManager(2, mockWordGraph, testAccessToken);
        
        assertDoesNotThrow(() -> {
            crawlManager.shutdownAndWait();
            crawlManager.shutdownAndWait(); // Should handle multiple calls gracefully
        });
    }

    @Test
    void testThreadPoolSizing() {
        assertDoesNotThrow(() -> {
            CrawlManager smallPool = new CrawlManager(1, mockWordGraph, testAccessToken);
            CrawlManager mediumPool = new CrawlManager(5, mockWordGraph, testAccessToken);
            CrawlManager largePool = new CrawlManager(20, mockWordGraph, testAccessToken);
            
            smallPool.shutdownAndWait();
            mediumPool.shutdownAndWait();
            largePool.shutdownAndWait();
        });
    }

    @Test
    void testConcurrentManagerInstances() {
        assertDoesNotThrow(() -> {
            CrawlManager manager1 = new CrawlManager(2, mockWordGraph, "token1");
            CrawlManager manager2 = new CrawlManager(3, mockWordGraph, "token2");
            
            Set<String> users1 = Set.of("user1", "user2");
            Set<String> users2 = Set.of("user3", "user4");
            Thread thread1 = new Thread(() -> manager1.crawlUsers(users1));
            Thread thread2 = new Thread(() -> manager2.crawlUsers(users2));
            
            thread1.start();
            thread2.start();
            
            thread1.join(5000); // Wait max 5 seconds
            thread2.join(5000);
        });
    }

    @Test
    void testLargeUserSet() {
        crawlManager = new CrawlManager(5, mockWordGraph, testAccessToken);

        Set<String> largeUserSet = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            largeUserSet.add("user" + i);
        }
        
        assertDoesNotThrow(() -> {
            crawlManager.crawlUsers(largeUserSet);
        });
    }

    @Test
    void testNullParameterHandling() {
        crawlManager = new CrawlManager(2, mockWordGraph, testAccessToken);

        assertThrows(Exception.class, () -> {
            crawlManager.crawlUsers(null);
        });
    }

    @Test
    void testUserSetWithNullElements() {
        crawlManager = new CrawlManager(2, mockWordGraph, testAccessToken);
        
        Set<String> usersWithNull = new HashSet<>();
        usersWithNull.add("validuser");
        usersWithNull.add(null);
        usersWithNull.add("anotheruser");

        assertDoesNotThrow(() -> {
            crawlManager.crawlUsers(usersWithNull);
        });
    }

    @Test
    void testUserSetWithEmptyStrings() {
        crawlManager = new CrawlManager(2, mockWordGraph, testAccessToken);
        
        Set<String> usersWithEmpty = Set.of("validuser", "", "   ", "anotheruser");
        
        assertDoesNotThrow(() -> {
            crawlManager.crawlUsers(usersWithEmpty);
        });
    }

    @Test
    void testResourceCleanup() {

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                CrawlManager manager = new CrawlManager(2, mockWordGraph, testAccessToken);
                manager.crawlUsers(Set.of("user" + i));
            }
        });
    }

    @Test
    void testInterruptionHandling() {
        crawlManager = new CrawlManager(2, mockWordGraph, testAccessToken);
        
        assertDoesNotThrow(() -> {
            Thread testThread = new Thread(() -> {
                crawlManager.crawlUsers(Set.of("user1", "user2"));
            });
            
            testThread.start();
            Thread.sleep(100); // Let it start
            testThread.interrupt(); // Interrupt it
            testThread.join(2000); // Wait for completion
        });
    }
} 