package org.example.twitter.service;

import org.example.twitter.model.Post;
import org.example.twitter.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private PostRepository postRepository;

    public void likePost(String postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow();
        post.getLikes().add(userId);
        postRepository.save(post);
    }

    public void unlikePost(String postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow();
        post.getLikes().remove(userId);
        postRepository.save(post);
    }
}
