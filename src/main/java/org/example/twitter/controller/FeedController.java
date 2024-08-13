package org.example.twitter.controller;

import org.example.twitter.model.Post;
import org.example.twitter.service.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/feed")
public class FeedController {

    @Autowired
    private FeedService feedService;

    @GetMapping("/{userId}")
    public List<Post> getUserFeed(@PathVariable String userId) {
        return feedService.getUserFeed(userId);
    }

    @GetMapping("/{userId}/next")
    public ResponseEntity<?> getNextFeed(@PathVariable String userId) {
        Optional<Post> postOptional = feedService.getNextFeedPost(userId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.ok("There is no feed for user " + userId);
        }

        return ResponseEntity.ok(postOptional.get());
    }

    @GetMapping("/{userId}/posts")
    public List<Post> getUserPosts(@PathVariable String userId,
                                   @RequestParam int page, @RequestParam int size) {
        return feedService.getUserPosts(userId, page, size);
    }

    @PostMapping("/{userId}/last/reset")
    public ResponseEntity<String> getUserPosts(@PathVariable String userId) {
        feedService.resetLastViewedPost(userId);
        return ResponseEntity.ok("Last viewed post reset");
    }
}
