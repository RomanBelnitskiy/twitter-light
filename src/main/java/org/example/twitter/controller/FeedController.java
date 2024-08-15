package org.example.twitter.controller;

import org.example.twitter.dto.PostDto;
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
    public ResponseEntity<List<PostDto>> getUserFeed(@PathVariable String userId) {
        return ResponseEntity.ok(feedService.getUserFeed(userId));
    }

    @GetMapping("/{userId}/next")
    public ResponseEntity<?> getNextFeed(@PathVariable String userId) {
        Optional<PostDto> postOptional = feedService.getNextFeedPost(userId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.ok("There is no feed for user " + userId);
        }

        return ResponseEntity.ok(postOptional.get());
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<List<PostDto>> getUserPosts(@PathVariable String userId,
                                   @RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(feedService.getUserPosts(userId, page, size));
    }
}
