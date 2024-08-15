package org.example.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.twitter.config.MongoDBTestcontainersConfig;
import org.example.twitter.config.RabbitMQTestcontainersConfig;
import org.example.twitter.model.Role;
import org.example.twitter.model.User;
import org.example.twitter.repository.UserRepository;
import org.example.twitter.security.AuthenticationRequest;
import org.example.twitter.security.AuthenticationResponse;
import org.example.twitter.security.JwtService;
import org.example.twitter.security.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({MongoDBTestcontainersConfig.class, RabbitMQTestcontainersConfig.class})
@Testcontainers
@ActiveProfiles("test")
class AuthControllerTest {

    @LocalServerPort
    private int port = 8080;

    private String baseUrl;

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
    private RabbitMQContainer rabbitMQContainer;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/auth";

    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testRabbitMQConnection() {
        System.out.println("container.getAmqpPort() = " + rabbitMQContainer.getAmqpPort());
        System.out.println("container.getHttpPort() = " + rabbitMQContainer.getHttpPort());
        Integer mappedPort = rabbitMQContainer.getMappedPort(5672); // Порт RabbitMQ для AMQP
        System.out.println("Mapped port for AMQP: " + mappedPort);
        assert(rabbitMQContainer.isRunning());
    }

    @Test
    @DisplayName("Successful registration path")
    void whenRegisterUser_ThenOk() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("user")
                .password("password")
                .email("user@example.com")
                .build();
        String stringRegisterRequest = objectMapper.writeValueAsString(registerRequest);

        MvcResult mvcResult = mvc.perform(post(baseUrl + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringRegisterRequest))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        AuthenticationResponse authenticationResponse = objectMapper.readValue(content, AuthenticationResponse.class);

        User user = userRepository.findByUsername(registerRequest.getUsername()).orElseThrow();

        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(registerRequest.getUsername());
        assertThat(user.getToken()).isEqualTo(authenticationResponse.getToken());
    }

    @Test
    @DisplayName("Register user with short password")
    void whenRegisterUserWithShortPassword_ThenReturnError() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("user")
                .password("pass")
                .email("user@example.com")
                .build();
        String stringRegisterRequest = objectMapper.writeValueAsString(registerRequest);

        mvc.perform(post(baseUrl + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringRegisterRequest))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors.[0]").value("The length of password must be at least 8 characters."));
    }

    @Test
    @DisplayName("Register user with null password")
    void whenRegisterUserWithNullPassword_ThenReturnError() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("user")
                .password(null)
                .email("user@example.com")
                .build();
        String stringRegisterRequest = objectMapper.writeValueAsString(registerRequest);

        mvc.perform(post(baseUrl + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringRegisterRequest))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors.[0]").value("The password is required."));
    }

    @Test
    @DisplayName("Register user with short username")
    void whenRegisterUserWithShortUsername_ThenReturnError() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("u")
                .password("password")
                .email("user@example.com")
                .build();
        String stringRegisterRequest = objectMapper.writeValueAsString(registerRequest);

        mvc.perform(post(baseUrl + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringRegisterRequest))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors.[0]").value("The length of username must be between 2 and 100 characters."));
    }

    @Test
    @DisplayName("Register user with null username")
    void whenRegisterUserWithNullUsername_ThenReturnError() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(null)
                .password("password")
                .email("user@example.com")
                .build();
        String stringRegisterRequest = objectMapper.writeValueAsString(registerRequest);

        mvc.perform(post(baseUrl + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringRegisterRequest))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors.[0]").value("The username is required."));
    }

    @Test
    @DisplayName("Register user with not valid email")
    void whenRegisterUserWithNotValidEmail_ThenReturnError() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("user1")
                .password("password")
                .email("@example.com")
                .build();

        String stringRegisterRequest = objectMapper.writeValueAsString(registerRequest);

        mvc.perform(post(baseUrl + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringRegisterRequest))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors.[0]").value("The email address is invalid."));
    }

    @Test
    @DisplayName("Register user with null email")
    void whenRegisterUserWithNullEmail_ThenReturnError() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("user1")
                .password("password")
                .email(null)
                .build();
        String stringRegisterRequest = objectMapper.writeValueAsString(registerRequest);

        mvc.perform(post(baseUrl + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringRegisterRequest))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors.[0]").value("The email address is required."));
    }

    @Test
    @DisplayName("Successful authentication path")
    void whenLoginUser_ThenOk() throws Exception {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .username("user")
                .password("password")
                .build();

        var user = User.builder()
                .username(authenticationRequest.getUsername())
                .password(passwordEncoder.encode(authenticationRequest.getPassword()))
                .email("user@example.com")
                .role(Role.USER)
                .build();

        userRepository.insert(user);

        String stringAuthenticationRequest = objectMapper.writeValueAsString(authenticationRequest);

        MvcResult mvcResult = mvc.perform(post(baseUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringAuthenticationRequest))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        AuthenticationResponse authenticationResponse = objectMapper.readValue(content, AuthenticationResponse.class);

        User userFromDB = userRepository.findByUsername(authenticationRequest.getUsername()).orElseThrow();

        assertThat(userFromDB.getToken()).isEqualTo(authenticationResponse.getToken());
    }

    @Test
    @DisplayName("Successful logout path")
    void whenLogoutUser_ThenOk() throws Exception {
        var user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .email("user@example.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);
        user.setToken(token);
        userRepository.insert(user);

        mvc.perform(post(baseUrl + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        User userFromDB = userRepository.findByUsername(user.getUsername()).orElseThrow();

        assertThat(userFromDB.getToken()).isNull();
    }

    @Test
    @DisplayName("Logout when user was logged out")
    void whenLogoutUserWhichWasLoggedOut_ThenReturnAccessDenied() throws Exception {
        var user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .email("user@example.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);
        userRepository.insert(user);

        mvc.perform(post(baseUrl + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Access Denied. Full authentication is required to access this resource")));

        User userFromDB = userRepository.findByUsername(user.getUsername()).orElseThrow();

        assertThat(userFromDB.getToken()).isNull();
    }
}