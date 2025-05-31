package org.platform.spidereddit.controller;

import lombok.RequiredArgsConstructor;
import org.platform.spidereddit.service.SpideredditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SpideredditController {

    private final SpideredditService spideredditService;

    @PostMapping("/crawl")
    public ResponseEntity<Map<String, Object>> crawl(@RequestBody Map<String, String> request) throws IOException {
        String url = request.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> graphData = spideredditService.crawlFromRedditUrl(url);
        return ResponseEntity.ok(graphData);
    }
}
