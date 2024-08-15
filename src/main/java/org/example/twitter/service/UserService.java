package org.example.twitter.service;

import lombok.RequiredArgsConstructor;
import org.example.twitter.dto.UserDto;
import org.example.twitter.mapper.UserMapper;
import org.example.twitter.model.User;
import org.example.twitter.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserDto> getFollowers(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return userRepository.findAllById(user.getSubscriptions()).stream()
                .map(userMapper::toDto)
                .toList();
    }

    public void followUser(String userId, String targetUserId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.getSubscriptions().add(targetUserId);
        userRepository.save(user);
    }
}
