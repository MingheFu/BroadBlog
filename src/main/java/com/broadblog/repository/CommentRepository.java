package com.broadblog.repository;

import java.util.List;

import com.broadblog.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAuthorId(Long authorId);
    List<Comment> findByPostId(Long postId);
} 