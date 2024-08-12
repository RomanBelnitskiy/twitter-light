package org.example.twitter.service;

import org.example.twitter.model.User;
import org.example.twitter.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("When follow user then target user id added to users subscriptions")
    public void whenFollowUser_thenTargetUserIdAddedToSubscriptions() {
        User user = User.builder()
                .id("user1")
                .build();
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        userService.followUser("user1", "user2");

        assertTrue(user.getSubscriptions().contains("user2"));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("When get user followers then return list of users")
    public void whenGetFollowers_thenReturnListOfUsers() {
        User user1 = User.builder()
                .id("user1")
                .build();
        User user2 = User.builder()
                .id("user2")
                .build();
        User user3 = User.builder()
                .id("user3")
                .build();
        user1.getSubscriptions().add(user2.getId());
        user1.getSubscriptions().add(user3.getId());
        when(userRepository.findById("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findAllById(user1.getSubscriptions())).thenReturn(List.of(user2, user3));

        List<User> followers = userService.getFollowers("user1");

        assertThat(followers)
                .isNotEmpty()
                .hasSize(2);
        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).findAllById(user1.getSubscriptions());
    }
}