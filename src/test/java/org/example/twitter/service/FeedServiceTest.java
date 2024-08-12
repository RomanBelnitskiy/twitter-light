package org.example.twitter.service;


import org.example.twitter.model.Post;
import org.example.twitter.model.User;
import org.example.twitter.repository.PostRepository;
import org.example.twitter.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedService feedService;

    @Nested
    @DisplayName("getUserFeed() tests")
    class GetUserFeed {
        @Test
        @DisplayName("When get user feed then return list of posts")
        void whenGetUserFeed_thenReturnListOfPosts() {
            String user1Id = "user1";
            String user2Id = "user2";
            String user3Id = "user3";
            User user1 = User.builder()
                    .id(user1Id)
                    .username("user1")
                    .subscriptions(Set.of(user2Id, user3Id))
                    .build();
            Post post1 = Post.builder()
                    .id("post1")
                    .userId("user2")
                    .content("content")
                    .createdAt(LocalDateTime.now())
                    .build();
            Post post2 = Post.builder()
                    .id("post1")
                    .userId("user3")
                    .content("content")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
            when(postRepository.findAllByUserIdInOrderByCreatedAtDesc(new ArrayList<>(user1.getSubscriptions())))
                    .thenReturn(List.of(post1, post2));

            List<Post> userFeed = feedService.getUserFeed(user1Id);

            assertThat(userFeed)
                    .hasSize(2);
            verify(userRepository, times(1)).findById(any(String.class));
            verify(postRepository, times(1)).findAllByUserIdInOrderByCreatedAtDesc(any(List.class));
        }

        @Test
        @DisplayName("When cant find user then throw exception")
        void whenCantFindUser_thenThrowException() {
            String user1Id = "user1";

            when(userRepository.findById(user1Id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> feedService.getUserFeed(user1Id))
                    .isInstanceOf(NoSuchElementException.class);
            verify(userRepository, times(1)).findById(any(String.class));
            verify(postRepository, times(0)).findAllByUserIdInOrderByCreatedAtDesc(any(List.class));
        }
    }

    @Nested
    @DisplayName("getUserPosts() tests")
    class GetUserPosts {
        @Test
        @DisplayName("When get user posts then return list of user's posts")
        void whenGetUserPosts_thenReturnListOfUserPosts() {
            String userId = "user1";
            Post post = Post.builder()
                    .id("post1")
                    .userId(userId)
                    .content("content")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(postRepository.findAllByUserId(userId, PageRequest.of(0, 10, Sort.by("createdAt").descending())))
                    .thenReturn(new PageImpl<>(List.of(post)));

            List<Post> userPosts = feedService.getUserPosts(userId, 0, 10);

            assertThat(userPosts)
                    .hasSize(1);
            verify(postRepository, times(1)).findAllByUserId(any(String.class), any(Pageable.class));
        }
    }
}