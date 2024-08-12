package org.example.twitter.service;

import lombok.RequiredArgsConstructor;
import org.example.twitter.dto.CommentDto;
import org.example.twitter.dto.NewPostDto;
import org.example.twitter.model.Comment;
import org.example.twitter.model.Post;
import org.example.twitter.repository.CommentRepository;
import org.example.twitter.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public void createPost(NewPostDto newPostDto) {
        Objects.requireNonNull(newPostDto, "Post cannot be null");

         Post post = Post.builder()
                .userId(newPostDto.getUserId())
                .content(newPostDto.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        postRepository.save(post);
    }

    public void addComment(String postId, CommentDto commentDto) {
        Objects.requireNonNull(postId, "PostId cannot be null");
        Objects.requireNonNull(commentDto, "Comment cannot be null");
        Comment comment = Comment.builder()
                .postId(postId)
                .userId(commentDto.getUserId())
                .content(commentDto.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);
    }

    public List<Comment> getComments(String postId) {
        return commentRepository.findAllByPostIdOrderByCreatedAtDesc(postId);
    }
}
