package com.broadblog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.broadblog.entity.CommentMessage;

@Repository
public interface CommentMessageRepository extends JpaRepository<CommentMessage, Long> {
    
    /**
     * 根据帖子ID查找评论
     */
    List<CommentMessage> findByPostIdOrderByCreatedAtDesc(String postId);
    
    /**
     * 根据作者ID查找评论
     */
    List<CommentMessage> findByAuthorIdOrderByCreatedAtDesc(String authorId);
    
    /**
     * 根据父评论ID查找回复
     */
    List<CommentMessage> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
    
    /**
     * 查找未删除的评论
     */
    List<CommentMessage> findByIsDeletedFalseOrderByCreatedAtDesc();
    
    /**
     * 根据帖子ID查找未删除的评论
     */
    List<CommentMessage> findByPostIdAndIsDeletedFalseOrderByCreatedAtDesc(String postId);
    
    /**
     * 统计帖子的评论数量
     */
    long countByPostIdAndIsDeletedFalse(String postId);
    
    /**
     * 根据类型查找评论
     */
    List<CommentMessage> findByTypeOrderByCreatedAtDesc(CommentMessage.MessageType type);
}
