package com.broadblog.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.broadblog.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorId(Long authorId);
    
    // 分页查询指定用户的帖子
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);
    
    // 按标题搜索（模糊匹配，不区分大小写）
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // 按内容搜索（模糊匹配，不区分大小写）
    Page<Post> findByContentContainingIgnoreCase(String content, Pageable pageable);
    
    // 按标题或内容搜索（复合搜索）
    @Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> findByTitleOrContentContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // 按标签搜索
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :tagName, '%'))")
    Page<Post> findByTagsNameContaining(@Param("tagName") String tagName, Pageable pageable);
    
    // 综合搜索（标题、内容、标签）
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchPosts(@Param("keyword") String keyword, Pageable pageable);
} 