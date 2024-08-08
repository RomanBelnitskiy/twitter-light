package org.example.twitter.controller;

import org.example.twitter.model.Post;
import org.example.twitter.service.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private FeedService feedService;

    @GetMapping("/{userId}")
    public List<Post> getUserFeed(@PathVariable String userId) {
        return feedService.getUserFeed(userId);
    }
    @GetMapping("/{userId}/posts")
    public List<Post> getUserPosts(@PathVariable String userId,
                                   @RequestParam int page, @RequestParam int size) {
        return feedService.getUserPosts(userId, page, size);
    }
}
