package org.example.twitter.mapper;

import org.example.twitter.dto.UserDto;
import org.example.twitter.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
