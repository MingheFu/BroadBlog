package com.broadblog.mapper.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.broadblog.entity.Post;

@Mapper
public interface PostMapper {
    
    // 基础CRUD操作
    List<Post> selectAll();
    Post selectById(@Param("id") Long id);
    int insert(Post post);
    int update(Post post);
    int deleteById(@Param("id") Long id);
    
    // 分页查询
    List<Post> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
    long countTotal();
    
    // 按作者查询
    List<Post> selectByAuthorId(@Param("authorId") Long authorId);
    List<Post> selectByAuthorIdWithPage(@Param("authorId") Long authorId, @Param("offset") int offset, @Param("limit") int limit);
    long countByAuthorId(@Param("authorId") Long authorId);
    
    // 搜索功能
    List<Post> searchByTitle(@Param("title") String title, @Param("offset") int offset, @Param("limit") int limit);
    List<Post> searchByContent(@Param("content") String content, @Param("offset") int offset, @Param("limit") int limit);
    List<Post> searchByTitleOrContent(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    List<Post> searchByTag(@Param("tagName") String tagName, @Param("offset") int offset, @Param("limit") int limit);
    List<Post> searchPosts(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    
    // 统计功能
    long countByTitle(@Param("title") String title);
    long countByContent(@Param("content") String content);
    long countByTitleOrContent(@Param("keyword") String keyword);
    long countByTag(@Param("tagName") String tagName);
    long countSearchResults(@Param("keyword") String keyword);
}
