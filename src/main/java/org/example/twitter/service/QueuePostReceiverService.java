package org.example.twitter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.twitter.model.Post;
import org.example.twitter.model.User;
import org.example.twitter.repository.UserRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueuePostReceiverService {

    @Value("${application.posts.queue.count}")
    private int queuePostCount;

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final UserRepository userRepository;
    private final ObjectMapper mapper;
    private final QueuePostSenderService queuePostSenderService;

    public Optional<Post> getPost(String userId) {
        if (Objects.requireNonNull(rabbitAdmin.getQueueInfo(userId)).getMessageCount() < (queuePostCount / 2)) {
            queuePostSenderService.sendPosts(userId);
        }

        Message message = rabbitTemplate.receive(userId);
        if (message != null) {
            String messageBody = new String(message.getBody());

            try {
                Post post = mapper.readValue(messageBody, Post.class);
                updateUserLastViewedPost(userId, post.getId());

                return Optional.of(post);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return Optional.empty();
    }

    private void updateUserLastViewedPost(String userId, String postId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setLastViewedPostId(postId);
        userRepository.save(user);
    }
}
