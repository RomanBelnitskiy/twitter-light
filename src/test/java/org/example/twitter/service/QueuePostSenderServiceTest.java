package org.example.twitter.service;

import org.bson.Document;
import org.example.twitter.model.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueuePostSenderServiceTest {

    @InjectMocks
    private QueuePostSenderService queuePostSenderService;

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("When getPostsAfterGivenId() with lastViewedPostId equal null then return two posts")
    void whenGetPostsAfterGivenIdAndLastViewedPostIdIsNull_thenShowTwoPosts() {
        String userId = "user1";
        List<String> subscriptions = List.of("user2", "user3");
        List<Post> postList = createPosts();

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("posts"), eq(Post.class)))
                .thenReturn(new AggregationResults<>(postList, new Document()));

        List<Post> posts = queuePostSenderService.getPostsAfterGivenId(userId, null,
                subscriptions, 10);

        assertThat(posts)
                .isNotNull()
                .hasSize(2);
        verify(mongoTemplate, never()).find(any(Query.class), eq(Post.class));
        verify(mongoTemplate, times(1)).aggregate(any(Aggregation.class), eq("posts"), eq(Post.class));
    }

    @Test
    @DisplayName("When getPostsAfterGivenId() then return one post")
    void whenGetPostsAfterGivenId_thenShowOnePost() {
        String userId = "user1";
        List<String> subscriptions = List.of("user2", "user3");
        Post post1 = createPost1();
        Post post2 = createPost2();

        String lastViewedPostId = post1.getId();

        when(mongoTemplate.find(any(Query.class), eq(Post.class))).thenReturn(List.of(post1));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("posts"), eq(Post.class)))
                .thenReturn(new AggregationResults<>(List.of(post2), new Document()));

        List<Post> posts = queuePostSenderService.getPostsAfterGivenId(userId, lastViewedPostId,
                subscriptions, 10);

        assertThat(posts)
                .isNotNull()
                .hasSize(1);
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Post.class));
        verify(mongoTemplate, times(1)).aggregate(any(Aggregation.class), eq("posts"), eq(Post.class));
    }

    @Test
    @DisplayName("When getPostsAfterGivenId() and can't find lastViewedPost then return empty list")
    void whenGetPostsAfterGivenIdAndCantFindLastViewedPost_thenReturnEmptyList() {
        String userId = "user1";
        List<String> subscriptions = List.of("user2", "user3");
        String lastViewedPostId = "post1";

        when(mongoTemplate.find(any(Query.class), eq(Post.class))).thenReturn(List.of());

        List<Post> posts = queuePostSenderService.getPostsAfterGivenId(userId, lastViewedPostId,
                subscriptions, 10);

        assertThat(posts)
                .isNotNull()
                .isEmpty();
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Post.class));
        verify(mongoTemplate, never()).aggregate(any(Aggregation.class), eq("posts"), eq(Post.class));
    }

    private Post createPost1() {
        return Post.builder()
                .id("post1")
                .userId("user2")
                .content("content1")
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
    }

    private Post createPost2() {
        return Post.builder()
                .id("post2")
                .userId("user3")
                .content("content2")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();
    }

    private List<Post> createPosts() {
        return List.of(
                createPost1(),
                createPost2()
        );
    }
}