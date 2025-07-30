package com.broadblog.repository;

import java.util.List;

import com.broadblog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    // You can add custom query methods here if needed
    List<Post> findByAuthorId(Long authorId);
} 