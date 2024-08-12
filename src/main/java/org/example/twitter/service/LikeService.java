package org.example.twitter.service;

import lombok.RequiredArgsConstructor;
import org.example.twitter.model.Post;
import org.example.twitter.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostRepository postRepository;

    public void likePost(String postId, String userId) {
        Objects.requireNonNull(postId);
        Objects.requireNonNull(userId);

        Post post = postRepository.findById(postId).orElseThrow();
        post.getLikes().add(userId);
        postRepository.save(post);
    }

    public void unlikePost(String postId, String userId) {
        Objects.requireNonNull(postId);
        Objects.requireNonNull(userId);

        Post post = postRepository.findById(postId).orElseThrow();
        post.getLikes().remove(userId);
        postRepository.save(post);
    }
}
