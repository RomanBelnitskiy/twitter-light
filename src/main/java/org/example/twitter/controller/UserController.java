package org.example.twitter.controller;

import org.example.twitter.model.User;
import org.example.twitter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}/followers")
    public List<User> getFollowers(@PathVariable String userId) {
        return userService.getFollowers(userId);
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<?> followUser(@PathVariable String userId, @RequestBody String targetUserId) {
        userService.followUser(userId, targetUserId);
        return ResponseEntity.ok("User followed successfully");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello from secured endpoint");
    }
}
