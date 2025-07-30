package com.broadblog.repository;

import java.util.Optional;

import com.broadblog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // You can add custom query methods here if needed
    // List<Comment> findByPostId(Long postId);
    //List<Comment> findByAuthorId(Long authorId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
 