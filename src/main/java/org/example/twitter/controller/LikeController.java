package org.example.twitter.controller;

import org.example.twitter.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> likePost(@PathVariable String postId, @RequestBody String userId) {
        likeService.likePost(postId, userId);
        return ResponseEntity.ok("Post liked successfully");
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<?> unlikePost(@PathVariable String postId, @RequestBody String userId) {
        likeService.unlikePost(postId, userId);
        return ResponseEntity.ok("Like removed successfully");
    }
}
