package org.example.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.twitter.config.MongoDBTestcontainersConfig;
import org.example.twitter.config.RabbitMQTestcontainersConfig;
import org.example.twitter.model.Post;
import org.example.twitter.model.Role;
import org.example.twitter.model.User;
import org.example.twitter.repository.PostRepository;
import org.example.twitter.repository.UserRepository;
import org.example.twitter.security.AuthenticationRequest;
import org.example.twitter.security.AuthenticationResponse;
import org.example.twitter.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({MongoDBTestcontainersConfig.class, RabbitMQTestcontainersConfig.class})
@Testcontainers
@ActiveProfiles("test")
class FeedControllerTest {

    @LocalServerPort
    private int port = 8080;

    private String baseUrl;
    private User user;

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "user@example.com";

    private static final String USERNAME2 = "user2";
    private static final String EMAIL2 = "user2@example.com";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/feed/";

        user = User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode(PASSWORD))
                .email(EMAIL)
                .role(Role.USER)
                .build();
        var token = jwtService.generateToken(user);
        user.setToken(token);
        String followedUserId = createUserAndThreePosts();
        user.getSubscriptions().add(followedUserId);
        user = userRepository.insert(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Get user feed")
    void whenGetUserFeed_ThenReturnThreePosts() throws Exception {
        mvc.perform(get(baseUrl + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + user.getToken()))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    @DisplayName("Get next post return one post and two posts has left in queue")
    void whenGetNextPost_ThenReturnOnePostAndHasLeftInQueue() throws Exception {
        setUserTokenToNull();
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        String stringAuthenticationRequest = objectMapper.writeValueAsString(authenticationRequest);

        MvcResult mvcResult = mvc.perform(post("http://localhost:" + port + "/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringAuthenticationRequest))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        AuthenticationResponse authenticationResponse = objectMapper.readValue(content, AuthenticationResponse.class);

        mvc.perform(get(baseUrl + user.getId() + "/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authenticationResponse.getToken()))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        assertThat(Objects.requireNonNull(rabbitAdmin.getQueueInfo(user.getId())).getMessageCount())
            .isEqualTo(2);
    }

    @Test
    @DisplayName("Get next post then no one post in queue")
    void whenGetNextPost_ThenNoOnePostInQueue() throws Exception {
        postRepository.deleteAll();
        setUserTokenToNull();
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        String stringAuthenticationRequest = objectMapper.writeValueAsString(authenticationRequest);

        MvcResult mvcResult = mvc.perform(post("http://localhost:" + port + "/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringAuthenticationRequest))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        AuthenticationResponse authenticationResponse = objectMapper.readValue(content, AuthenticationResponse.class);

        mvc.perform(get(baseUrl + user.getId() + "/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authenticationResponse.getToken()))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", is("There is no feed for user " + user.getId())));

        assertThat(Objects.requireNonNull(rabbitAdmin.getQueueInfo(user.getId())).getMessageCount())
                .isEqualTo(0);
    }

    @Test
    @DisplayName("Get user's posts returns three posts")
    void whenGetUserPosts_ThenReturnThreePosts() throws Exception {
        createThreePostsFroUser(user.getId());

        mvc.perform(get(baseUrl + user.getId() + "/posts?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + user.getToken()))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.length()", is(3)));
    }

    private void setUserTokenToNull() {
        user = userRepository.findById(user.getId()).orElseThrow();
        user.setToken(null);
        user = userRepository.save(user);
    }

    private String createUserAndThreePosts() {
        User followedUser = User.builder()
                .username(USERNAME2)
                .password(passwordEncoder.encode(PASSWORD))
                .email(EMAIL2)
                .role(Role.USER)
                .build();

        followedUser = userRepository.insert(followedUser);

        createThreePostsFroUser(followedUser.getId());

        return followedUser.getId();
    }

    private void createThreePostsFroUser(String userId) {
        Post post1 = Post.builder()
                .userId(userId)
                .content("Post #1 content")
                .createdAt(LocalDateTime.now())
                .build();
        postRepository.insert(post1);

        Post post2 = Post.builder()
                .userId(userId)
                .content("Post #2 content")
                .createdAt(LocalDateTime.now())
                .build();
        postRepository.insert(post2);

        Post post3 = Post.builder()
                .userId(userId)
                .content("Post #3 content")
                .createdAt(LocalDateTime.now())
                .build();
        postRepository.insert(post3);
    }
}