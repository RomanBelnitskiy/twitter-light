package org.example.twitter.repository;

import org.example.twitter.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findAllByUserIdInOrderByCreatedAtDesc(List<String> subscriptions);

    Page<Post> findAllByUserId(String userId, Pageable pageable);
}
