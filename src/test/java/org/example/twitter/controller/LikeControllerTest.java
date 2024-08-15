package org.example.twitter.controller;

import org.example.twitter.config.MongoDBTestcontainersConfig;
import org.example.twitter.model.Post;
import org.example.twitter.model.Role;
import org.example.twitter.model.User;
import org.example.twitter.repository.PostRepository;
import org.example.twitter.repository.UserRepository;
import org.example.twitter.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({MongoDBTestcontainersConfig.class})
@Testcontainers
@ActiveProfiles("test")
class LikeControllerTest {

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
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/posts";

        user = User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode(PASSWORD))
                .email(EMAIL)
                .role(Role.USER)
                .build();
        var token = jwtService.generateToken(user);
        user.setToken(token);
        user = userRepository.insert(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("When user likes the post then ok")
    void whenUserLikesPost_ThenOk() throws Exception {
        Post post = createUserAndPost();

        mvc.perform(post(baseUrl + "/" + post.getId() + "/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + user.getToken())
                        .content(user.getId()))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", is("Post liked successfully")));
    }

    @Test
    @DisplayName("When user unlikes the post then ok")
    void whenUserUnlikesPost_ThenOk() throws Exception {
        Post post = createUserAndPostWithLike();

        mvc.perform(delete(baseUrl + "/" + post.getId() + "/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + user.getToken())
                        .content(user.getId()))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", is("Like removed successfully")));
    }

    private User createUser() {
        return userRepository.insert(
                User.builder()
                        .username(USERNAME2)
                        .password(passwordEncoder.encode(PASSWORD))
                        .email(EMAIL2)
                        .role(Role.USER)
                        .build()
        );
    }

    private Post createUserAndPost() {
        User otherUser = createUser();

        Post post = Post.builder()
                .userId(otherUser.getId())
                .content("Post #1 content")
                .createdAt(LocalDateTime.now())
                .build();
        return postRepository.insert(post);
    }

    private Post createUserAndPostWithLike() {
        User otherUser = createUser();

        Post post = Post.builder()
                .userId(otherUser.getId())
                .content("Post #1 content")
                .likes(Set.of(user.getId()))
                .createdAt(LocalDateTime.now())
                .build();
        return postRepository.insert(post);
    }

}