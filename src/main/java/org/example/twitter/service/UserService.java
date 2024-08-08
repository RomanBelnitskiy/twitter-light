package org.example.twitter.service;

import org.example.twitter.dto.UserDto;
import org.example.twitter.model.User;
import org.example.twitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void register(UserDto userDto) {

    }

    public List<User> getFollowers(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return userRepository.findAllById(user.getSubscriptions());
    }

    public void followUser(String userId, String targetUserId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.getSubscriptions().add(targetUserId);
        userRepository.save(user);
    }
}
