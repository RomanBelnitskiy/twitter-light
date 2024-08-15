package org.example.twitter.service;

import lombok.RequiredArgsConstructor;
import org.example.twitter.dto.PostDto;
import org.example.twitter.mapper.PostMapper;
import org.example.twitter.model.Post;
import org.example.twitter.model.User;
import org.example.twitter.repository.PostRepository;
import org.example.twitter.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final QueuePostReceiverService queuePostReceiverService;
    private final PostMapper postMapper;

    public List<PostDto> getUserFeed(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<String> subscriptions = new ArrayList<>(user.getSubscriptions());

        return postRepository.findAllByUserIdInOrderByCreatedAtDesc(subscriptions)
                .stream()
                .map(postMapper::toPostDto)
                .toList();
    }

    public Optional<PostDto> getNextFeedPost(String userId) {
        return queuePostReceiverService.getPost(userId)
                .map(postMapper::toPostDto);
    }

    public List<PostDto> getUserPosts(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findAllByUserId(userId, pageable).getContent()
                .stream()
                .map(postMapper::toPostDto)
                .toList();
    }
}
