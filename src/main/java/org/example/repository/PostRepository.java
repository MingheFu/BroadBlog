package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    // You can add custom query methods here if needed
} 