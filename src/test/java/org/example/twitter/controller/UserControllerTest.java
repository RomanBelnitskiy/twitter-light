package org.example.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.twitter.config.MongoDBTestcontainersConfig;
import org.example.twitter.dto.UserDto;
import org.example.twitter.model.Role;
import org.example.twitter.model.User;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(MongoDBTestcontainersConfig.class)
@ActiveProfiles("test")
class UserControllerTest {

    @LocalServerPort
    private int port = 8080;

    private String baseUrl;
    private User user;

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "user@example.com";

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

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/users/";

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
    @DisplayName("Get one follower")
    void whenGetFollowers_ThenReturnOneUser() throws Exception {
        User follower = User.builder()
                .username("user1")
                .password(passwordEncoder.encode(PASSWORD))
                .email("user1@example.com")
                .role(Role.USER)
                .build();
        follower = userRepository.insert(follower);
        user.getSubscriptions().add(follower.getId());
        userRepository.save(user);

        MvcResult mvcResult = mvc.perform(get(baseUrl + user.getId() + "/followers")
                        .header("Authorization", "Bearer " + user.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        List<UserDto> users = Arrays.asList(objectMapper.readValue(content, UserDto[].class));

        assertThat(users)
                .isNotNull()
                .hasSize(1);
    }

    @Test
    @DisplayName("follow user")
    void whenFollowUser_ThenOk() throws Exception {
        User following = User.builder()
                .username("user1")
                .password(passwordEncoder.encode(PASSWORD))
                .email("user1@example.com")
                .role(Role.USER)
                .build();
        following = userRepository.insert(following);

        MvcResult mvcResult = mvc.perform(post(baseUrl + user.getId() + "/follow")
                        .header("Authorization", "Bearer " + user.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(following.getId()))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();

        assertThat(content)
                .isNotNull()
                .contains("User followed successfully");

        Set<String> subscriptions = userRepository.findById(user.getId()).orElseThrow().getSubscriptions();
        assertThat(subscriptions)
                .isNotNull()
                .hasSize(1)
                .containsExactly(following.getId());
    }
}