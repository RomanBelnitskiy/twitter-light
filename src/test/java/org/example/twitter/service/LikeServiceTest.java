package org.example.twitter.service;

import org.example.twitter.model.Post;
import org.example.twitter.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private LikeService likeService;

    @Nested
    @DisplayName("likePost() tests")
    class LikePost {
        @Test
        @DisplayName("When like post then added user id to post likes")
        void whenLikePost_thenUserIdAddedToPostLikes() {
            String likerId = "user2";
            Post post = Post.builder()
                    .id("post1")
                    .userId("user1")
                    .content("content")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
            when(postRepository.save(post)).thenReturn(post);

            likeService.likePost(post.getId(), likerId);

            assertThat(post.getLikes())
                    .isNotNull()
                    .hasSize(1);
            verify(postRepository, times(1)).findById(post.getId());
            verify(postRepository, times(1)).save(post);
        }

        @Test
        @DisplayName("When post for like doesn't exist then throw exception")
        void whenPostForLikeDoesntExist_thenThrowException() {
            String likerId = "user2";
            String postId = "post1";

            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeService.likePost(postId, likerId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("No value present");
            verify(postRepository, times(1)).findById(postId);
            verify(postRepository, times(0)).save(any(Post.class));
        }

        @Test
        @DisplayName("When post id is null then throw exception")
        void whenPostIdIsNull_thenThrowException() {
            String likerId = "user2";

            assertThatThrownBy(() -> likeService.likePost(null, likerId))
                    .isInstanceOf(NullPointerException.class);
            verify(postRepository, times(0)).findById(any(String.class));
            verify(postRepository, times(0)).save(any(Post.class));
        }

        @Test
        @DisplayName("When user id is null then throw exception")
        void whenUserIdIsNull_thenThrowException() {
            String postId = "post1";

            assertThatThrownBy(() -> likeService.likePost(postId, null))
                    .isInstanceOf(NullPointerException.class);
            verify(postRepository, times(0)).findById(any(String.class));
            verify(postRepository, times(0)).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("unlikePost() tests")
    class UnlikePost {
        @Test
        @DisplayName("When unlike post then removed user id from post likes")
        void whenUnlikePost_thenUserIdRemovedFromPostLikes() {
            String likerId = "user2";
            Post post = Post.builder()
                    .id("post1")
                    .userId("user1")
                    .content("content")
                    .createdAt(LocalDateTime.now())
                    .build();
            post.getLikes().add(likerId);

            when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
            when(postRepository.save(post)).thenReturn(post);

            likeService.unlikePost(post.getId(), likerId);

            assertThat(post.getLikes())
                    .isNotNull()
                    .hasSize(0);
            verify(postRepository, times(1)).findById(post.getId());
            verify(postRepository, times(1)).save(post);
        }

        @Test
        @DisplayName("When post for unlike doesn't exist then throw exception")
        void whenPostForUnlikeDoesntExist_thenThrowException() {
            String likerId = "user2";
            String postId = "post1";

            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeService.unlikePost(postId, likerId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("No value present");
            verify(postRepository, times(1)).findById(postId);
            verify(postRepository, times(0)).save(any(Post.class));
        }

        @Test
        @DisplayName("When post id is null then throw exception")
        void whenPostIdIsNull_thenThrowException() {
            String likerId = "user2";

            assertThatThrownBy(() -> likeService.unlikePost(null, likerId))
                    .isInstanceOf(NullPointerException.class);
            verify(postRepository, times(0)).findById(any(String.class));
            verify(postRepository, times(0)).save(any(Post.class));
        }

        @Test
        @DisplayName("When user id is null then throw exception")
        void whenUserIdIsNull_thenThrowException() {
            String postId = "post1";

            assertThatThrownBy(() -> likeService.unlikePost(postId, null))
                    .isInstanceOf(NullPointerException.class);
            verify(postRepository, times(0)).findById(any(String.class));
            verify(postRepository, times(0)).save(any(Post.class));
        }
    }

}