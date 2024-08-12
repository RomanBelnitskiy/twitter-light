package org.example.twitter.service;

import org.example.twitter.dto.CommentDto;
import org.example.twitter.dto.NewPostDto;
import org.example.twitter.model.Comment;
import org.example.twitter.model.Post;
import org.example.twitter.repository.CommentRepository;
import org.example.twitter.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    private PostService postService;

    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, commentRepository);
    }

    @Nested
    @DisplayName("createPost() tests")
    class CreatePost {
        @Test
        @DisplayName("When post is null then throw NPE")
        void whenPostIsNull_thenExceptionIsThrown() {
            Exception exception = assertThrows(
                    NullPointerException.class,
                    () -> postService.createPost(null));

            String expectedMessage = "Post cannot be null";
            String actualMessage = exception.getMessage();

            assertEquals(expectedMessage, actualMessage);
        }

        @Test
        @DisplayName("When create post then save it to DB")
        void whenCreatePost_thenSaveToDB() {
            NewPostDto newPostDto = new NewPostDto("userId", "content");

            postService.createPost(newPostDto);

            verify(postRepository, times(1)).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("addComment() tests")
    class AddComment {
        @Test
        @DisplayName("When user id is null then throw NPE")
        void whenUserIdIsNull_thenExceptionIsThrown() {
            Exception exception = assertThrows(
                    NullPointerException.class,
                    () -> postService.addComment(null, new CommentDto()));

            String expectedMessage = "PostId cannot be null";
            String actualMessage = exception.getMessage();

            assertEquals(expectedMessage, actualMessage);
        }

        @Test
        @DisplayName("When comment is null then throw NPE")
        void whenCommentIsNull_thenExceptionIsThrown() {
            Exception exception = assertThrows(
                    NullPointerException.class,
                    () -> postService.addComment("", null));

            String expectedMessage = "Comment cannot be null";
            String actualMessage = exception.getMessage();

            assertEquals(expectedMessage, actualMessage);
        }

        @Test
        @DisplayName("When create comment then save it to DB")
        void whenCreateComment_thenSaveToDB() {
            CommentDto newCommentDto = CommentDto.builder()
                    .userId("userId")
                    .content("content")
                    .build();

            postService.addComment("userId", newCommentDto);

            verify(commentRepository, times(1)).save(any(Comment.class));
        }
    }
}