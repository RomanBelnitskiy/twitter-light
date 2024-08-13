package org.example.twitter.service;

import lombok.RequiredArgsConstructor;
import org.example.twitter.model.Post;
import org.example.twitter.model.User;
import org.example.twitter.repository.PostRepository;
import org.example.twitter.repository.UserRepository;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueuePostSenderService {

    @Value("${application.posts.queue.count}")
    private int queuePostCount;

    private final RabbitTemplate rabbitTemplate;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final DirectExchange directExchange;
    private final RabbitAdmin rabbitAdmin;

    public void sendPosts(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<String> subscriptions = new ArrayList<>(user.getSubscriptions());
        List<Post> posts = getPostsAfterGivenId(user.getId(), user.getLastViewedPostId(), subscriptions, queuePostCount);
        if (!posts.isEmpty()) {
            rabbitAdmin.purgeQueue(user.getId(), false);
            for (Post post : posts) {
                rabbitTemplate.convertAndSend(directExchange.getName(), user.getId(), post);
            }
        }
    }

    public List<Post> getPostsAfterGivenId(String userId, String lastViewedPostId, List<String> userIds, int limit) {

        MatchOperation matchUsers = Aggregation.match(Criteria.where("userId").in(userIds));
        SortOperation sortByDate = Aggregation.sort(org.springframework.data.domain.Sort.by("createdAt").ascending());

        final Aggregation aggregationToFetchPosts;
        if (lastViewedPostId != null) {
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(lastViewedPostId));
            List<Post> foundPosts = mongoTemplate.find(query, Post.class);
            if (foundPosts.isEmpty()) {
                return List.of();
            }

            Post foundPost = foundPosts.get(0);
            aggregationToFetchPosts = Aggregation.newAggregation(
                    matchUsers,
                    sortByDate,
                    Aggregation.match(Criteria.where("createdAt").gt(foundPost.getCreatedAt()).and("userId").ne(userId)),
                    Aggregation.limit(limit)
            );
        } else {
            aggregationToFetchPosts = Aggregation.newAggregation(
                    matchUsers,
                    sortByDate,
                    Aggregation.match(Criteria.where("userId").ne(userId)),
                    Aggregation.limit(limit)
            );
        }

        AggregationResults<Post> results = mongoTemplate.aggregate(aggregationToFetchPosts, "posts", Post.class);

        return results.getMappedResults();
    }
}
