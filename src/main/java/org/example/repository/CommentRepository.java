package org.example.repository;

import java.util.List;

import org.example.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // You can add custom query methods here if needed
    List<Comment> findByPostId(Long postId);
    List<Comment> findByAuthorId(Long authorId);
} 