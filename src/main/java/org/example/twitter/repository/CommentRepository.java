package org.example.twitter.repository;

import org.example.twitter.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findAllByPostIdOrderByCreatedAtDesc(String postId);
}
