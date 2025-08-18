package com.broadblog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.broadblog.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAuthorId(Long authorId);
    List<Comment> findByPostId(Long postId);
    List<Comment> findByParentComment_Id(Long parentCommentId);
    long countByPostId(Long postId);
} 