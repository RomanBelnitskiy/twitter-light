package org.example.twitter.service;

import org.example.twitter.model.User;
import org.example.twitter.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testFollowUser() {
        User user = new User();
        user.setId("user1");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        userService.followUser("user1", "user2");
        assertTrue(user.getSubscriptions().contains("user2"));
    }
}