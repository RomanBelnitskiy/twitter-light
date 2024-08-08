package org.example.twitter.service;

import org.example.twitter.model.Post;
import org.example.twitter.model.User;
import org.example.twitter.repository.PostRepository;
import org.example.twitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FeedService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Post> getUserFeed(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<String> subscriptions = new ArrayList<>(user.getSubscriptions());
        return postRepository.findAllByUserIdInOrderByCreatedAtDesc(subscriptions);
    }

    public List<Post> getUserPosts(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findAllByUserId(userId, pageable).getContent();
    }
}
