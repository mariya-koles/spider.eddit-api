
# Spidereddit API

Spidereddit is a multithreaded Java-based backend API that analyzes Reddit comment threads to build a word co-occurrence graph from user histories. It is designed to serve a frontend that visualizes relationships between commonly associated words in Reddit conversations.

## Features

- Accepts a Reddit thread URL and OAuth token
- Fetches top-level commenters
- Retrieves historical posts and comments for each user
- Filters text by part-of-speech (POS) tagging
- Constructs a word co-occurrence graph
- Returns structured data for visualization

## Tech Stack

- Java 17
- Spring Boot
- OkHttp (Reddit API requests)
- Jackson (JSON parsing)
- Apache Lucene (Stop words and token filtering)
- Multithreading with `Runnable` tasks
- RESTful API
- Maven

## API Endpoints

### `POST /crawl`

Initiates a crawl of a Reddit thread and generates a word graph.

#### Request Body

```json
{
  "url": "https://www.reddit.com/r/example/comments/threadid/example_title/",
  "token": "your_oauth_token_here"
}
```

#### Response

```json
{
  "nodes": [{ "id": "word1", "value": 15 }, ...],
  "links": [{ "source": "word1", "target": "word2", "value": 3 }, ...]
}
```

## How It Works

1. A POST request to `/crawl` triggers the RedditClient to fetch thread commenters.
2. Each user's history is fetched in parallel using the `UserHistoryFetcher`.
3. Text is tokenized and filtered using Lucene and POS tagging.
4. A graph is built using co-occurrence of filtered words.
5. Graph is serialized to JSON for frontend visualization.

## Setup

1. Clone the repository
2. Configure your Reddit OAuth credentials in `RedditConfig`
3. Build the project using Maven:
   ```bash
   mvn clean install
   ```
4. Run the Spring Boot app:
   ```bash
   mvn spring-boot:run
   ```

