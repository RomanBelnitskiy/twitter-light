package org.example.twitter.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostDto {
    private String id;
    private String userId;
    private String content;
    private LocalDateTime createdAt;
    private int likes;
}
