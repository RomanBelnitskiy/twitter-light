package org.example.twitter.service;

import org.example.twitter.dto.CommentDto;
import org.example.twitter.dto.PostDto;
import org.example.twitter.model.Comment;
import org.example.twitter.model.Post;
import org.example.twitter.repository.CommentRepository;
import org.example.twitter.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;

    public void createPost(PostDto postDto) {
        Post post = new Post();
        post.setUserId(postDto.getUserId());
        post.setContent(postDto.getContent());
        post.setCreatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    public void addComment(String postId, CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(commentDto.getUserId());
        comment.setContent(commentDto.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public List<Comment> getComments(String postId) {
        return commentRepository.findAllByPostIdOrderByCreatedAtDesc(postId);
    }
}
