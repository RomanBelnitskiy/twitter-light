package org.example.twitter.controller;

import org.example.twitter.dto.CommentDto;
import org.example.twitter.dto.PostDto;
import org.example.twitter.model.Comment;
import org.example.twitter.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostDto postDto) {
        postService.createPost(postDto);
        return ResponseEntity.ok("Post created successfully");
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable String postId, @RequestBody CommentDto commentDto) {
        postService.addComment(postId, commentDto);
        return ResponseEntity.ok("Comment added successfully");
    }

    @GetMapping("/{postId}/comments")
    public List<Comment> getComments(@PathVariable String postId) {
        return postService.getComments(postId);
    }
}
