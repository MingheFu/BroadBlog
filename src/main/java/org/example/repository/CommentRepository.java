package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.Comment;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // You can add custom query methods here if needed
    List<Comment> findByPostId(Long postId);
    List<Comment> findByAuthorId(Long authorId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
} 