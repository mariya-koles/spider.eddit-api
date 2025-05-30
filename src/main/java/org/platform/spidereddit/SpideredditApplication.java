package org.platform.spidereddit;

import org.platform.spidereddit.reddit.RedditClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class SpideredditApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(SpideredditApplication.class, args);

        RedditClient client = new RedditClient();
        String postId = client.extractPostId("");
        Set<String> commenters = client.getTopLevelCommenters(postId);
        System.out.println(commenters);




    }

}
