package org.example.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.twitter.config.MongoDBTestcontainersConfig;
import org.example.twitter.dto.CommentDto;
import org.example.twitter.dto.NewPostDto;
import org.example.twitter.model.Comment;
import org.example.twitter.model.Post;
import org.example.twitter.model.Role;
import org.example.twitter.model.User;
import org.example.twitter.repository.CommentRepository;
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

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class PostControllerTest {

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
    private CommentRepository commentRepository;

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
    @DisplayName("When user create new post then ok")
    void whenUserCreateNewPost_ThenOk() throws Exception {
        NewPostDto newPostDto = new NewPostDto(user.getId(), "Post content");
        String postRequest = objectMapper.writeValueAsString(newPostDto);

        mvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + user.getToken())
                        .content(postRequest))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", is("Post created successfully")));
    }

    @Test
    @DisplayName("When user add comment to post then ok")
    void whenUserAddCommentToPost_ThenOk() throws Exception {
        Post post = createUserAndPost();

        CommentDto commentDto = CommentDto.builder()
                .userId(user.getId())
                .content("Comment content")
                .build();
        String commentRequest = objectMapper.writeValueAsString(commentDto);

        mvc.perform(post(baseUrl + "/" + post.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + user.getToken())
                        .content(commentRequest))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", is("Comment added successfully")));
    }

    @Test
    @DisplayName("When user get post comments then return list of two comments")
    void whenUserGetPostComments_ThenListOfTwoComments() throws Exception {
        Post post = createUserAndPost();
        createTwoCommentsFor(post);

        mvc.perform(get(baseUrl + "/" + post.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + user.getToken()))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.length()", is(2)));
    }

    private Post createUserAndPost() {
        User otherUser = User.builder()
                .username(USERNAME2)
                .password(passwordEncoder.encode(PASSWORD))
                .email(EMAIL2)
                .role(Role.USER)
                .build();
        otherUser = userRepository.insert(otherUser);

        Post post = Post.builder()
                .userId(otherUser.getId())
                .content("Post #1 content")
                .createdAt(LocalDateTime.now())
                .build();
        return postRepository.insert(post);
    }

    private void createTwoCommentsFor(Post post) {
        Comment comment1 = Comment.builder()
                .postId(post.getId())
                .userId(user.getId())
                .content("Comment #1 content")
                .createdAt(LocalDateTime.now())
                .build();
        commentRepository.insert(comment1);

        Comment comment2 = Comment.builder()
                .postId(post.getId())
                .userId(user.getId())
                .content("Comment #2 content")
                .createdAt(LocalDateTime.now())
                .build();
        commentRepository.insert(comment2);
    }
}