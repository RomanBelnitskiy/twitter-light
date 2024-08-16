package org.example.twitter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.twitter.model.Post;
import org.example.twitter.model.Role;
import org.example.twitter.model.User;
import org.example.twitter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueuePostReceiverServiceTest {

    @Mock
    private RabbitAdmin rabbitAdmin;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private UserRepository userRepository;

    private ObjectMapper mapper;

    @Mock
    private QueuePostSenderService queuePostSenderService;

    @InjectMocks
    private QueuePostReceiverService queuePostReceiverService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        queuePostReceiverService = new QueuePostReceiverService(
                rabbitAdmin,
                rabbitTemplate,
                userRepository,
                mapper,
                queuePostSenderService
        );
        ReflectionTestUtils.setField(queuePostReceiverService, "queuePostCount", 10);
    }

    @Test
    @DisplayName("When get post then return post")
    void whenGetPost_thenReturnPost() throws JsonProcessingException {
        User user = createUser();
        Post post = createPost();
        Message message = createMessage(post);

        when(rabbitAdmin.getQueueInfo(user.getId())).thenReturn(new QueueInformation(user.getId(), 10, 1));
        when(rabbitTemplate.receive(user.getId())).thenReturn(message);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Optional<Post> postOptional = queuePostReceiverService.getPost(user.getId());

        assertThat(postOptional)
            .isPresent();
        verify(queuePostSenderService, never()).sendPosts(anyString());
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("When get post then sendPosts() and return post")
    void whenGetPost_thenSendPostsAndReturnPost() throws JsonProcessingException {
        User user = createUser();
        Post post = createPost();
        Message message = createMessage(post);

        when(rabbitAdmin.getQueueInfo(user.getId())).thenReturn(new QueueInformation(user.getId(), 4, 1));
        when(rabbitTemplate.receive(user.getId())).thenReturn(message);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Optional<Post> postOptional = queuePostReceiverService.getPost(user.getId());

        assertThat(postOptional)
                .isPresent();
        verify(queuePostSenderService, times(1)).sendPosts(user.getId());
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("When get post and message is null then return optional empty")
    void whenGetPostAndMessageIsNull_thenReturnOptionalEmpty() {
        User user = createUser();

        when(rabbitAdmin.getQueueInfo(user.getId())).thenReturn(new QueueInformation(user.getId(), 10, 1));
        when(rabbitTemplate.receive(user.getId())).thenReturn(null);

        Optional<Post> postOptional = queuePostReceiverService.getPost(user.getId());

        assertThat(postOptional)
                .isEmpty();
        verify(queuePostSenderService, never()).sendPosts(anyString());
        verify(userRepository, never()).findById(user.getId());
        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("When get post and can't deserialize message body then throw exception")
    void whenGetPostAndCantGetMessageBody_thenThrowException() {
        User user = createUser();
        Message message = new Message("".getBytes());

        when(rabbitAdmin.getQueueInfo(user.getId())).thenReturn(new QueueInformation(user.getId(), 10, 1));
        when(rabbitTemplate.receive(user.getId())).thenReturn(message);

        assertThatThrownBy(() -> queuePostReceiverService.getPost(user.getId()))
                .isInstanceOf(RuntimeException.class);

        verify(queuePostSenderService, never()).sendPosts(anyString());
        verify(userRepository, never()).findById(user.getId());
        verify(userRepository, never()).save(user);
    }

    private User createUser() {
        return User.builder()
                .id("userId")
                .username("username")
                .email("user@example.com")
                .role(Role.USER)
                .build();
    }

    private Message createMessage(Post post) throws JsonProcessingException {
        String postAsString = mapper.writeValueAsString(post);
        return new Message(postAsString.getBytes());
    }

    private Post createPost() {
        return Post.builder()
                .id("postId")
                .userId("user2Id")
                .content("Post content")
                .createdAt(LocalDateTime.now().minusHours(3))
                .build();
    }
}