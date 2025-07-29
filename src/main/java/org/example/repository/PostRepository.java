package org.example.repository;

import java.util.List;

import org.example.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    // You can add custom query methods here if needed
    List<Post> findByAuthorId(Long authorId);
} 