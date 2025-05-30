package org.platform.spidereddit.reddit;

import io.github.cdimascio.dotenv.Dotenv;

public class RedditConfig {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static final String CLIENT_ID = dotenv.get("REDDIT_CLIENT_ID");
    public static final String CLIENT_SECRET = dotenv.get("REDDIT_CLIENT_SECRET");
    public static final String USERNAME = dotenv.get("REDDIT_USERNAME");
    public static final String PASSWORD = dotenv.get("REDDIT_PASSWORD");
    public static final String USER_AGENT = dotenv.get("REDDIT_USER_AGENT");
}
