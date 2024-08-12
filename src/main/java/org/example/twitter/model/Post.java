package org.example.twitter.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "posts")
@Builder
public class Post {
    @Id
    private String id;
    @Indexed
    private String userId;
    private String content;
    private LocalDateTime createdAt;
    @Builder.Default
    private Set<String> likes = new HashSet<>();
}
