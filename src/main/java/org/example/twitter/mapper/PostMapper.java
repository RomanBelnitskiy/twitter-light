package org.example.twitter.mapper;

import org.example.twitter.dto.PostDto;
import org.example.twitter.model.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {
    public PostDto toPostDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .content(post.getContent())
                .likes(post.getLikes().size())
                .build();
    }
}
