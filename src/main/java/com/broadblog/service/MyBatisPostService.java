package com.broadblog.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.broadblog.entity.Post;
import com.broadblog.mapper.mybatis.PostMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("myBatisPostService")
public class MyBatisPostService {
    
    @Autowired
    private PostMapper postMapper;
    
    /**
     * 获取所有文章（分页）
     */
    public PageInfo<Post> getPostsByPage(int page, int size) {
        PageHelper.startPage(page, size);
        List<Post> posts = postMapper.selectAll();
        return new PageInfo<>(posts);
    }
    
    /**
     * 根据ID获取文章
     */
    public Optional<Post> getPostById(Long id) {
        Post post = postMapper.selectById(id);
        return Optional.ofNullable(post);
    }
    
    /**
     * 保存文章
     */
    @Transactional
    public Post savePost(Post post) {
        if (post.getId() == null) {
            // 新增
            postMapper.insert(post);
        } else {
            // 更新
            postMapper.update(post);
        }
        return post;
    }
    
    /**
     * 删除文章
     */
    @Transactional
    public void deletePost(Long id) {
        postMapper.deleteById(id);
    }
    
    /**
     * 根据作者ID获取文章（分页）
     */
    public PageInfo<Post> getPostsByAuthorIdWithPage(Long authorId, int page, int size) {
        PageHelper.startPage(page, size);
        List<Post> posts = postMapper.selectByAuthorId(authorId);
        return new PageInfo<>(posts);
    }
    
    /**
     * 按标题搜索
     */
    public PageInfo<Post> searchByTitle(String title, int page, int size) {
        PageHelper.startPage(page, size);
        List<Post> posts = postMapper.searchByTitle(title, 0, size); // PageHelper会自动处理分页
        return new PageInfo<>(posts);
    }
    
    /**
     * 按内容搜索
     */
    public PageInfo<Post> searchByContent(String content, int page, int size) {
        PageHelper.startPage(page, size);
        List<Post> posts = postMapper.searchByContent(content, 0, size);
        return new PageInfo<>(posts);
    }
    
    /**
     * 按标题或内容搜索
     */
    public PageInfo<Post> searchByTitleOrContent(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<Post> posts = postMapper.searchByTitleOrContent(keyword, 0, size);
        return new PageInfo<>(posts);
    }
    
    /**
     * 按标签搜索
     */
    public PageInfo<Post> searchByTag(String tagName, int page, int size) {
        PageHelper.startPage(page, size);
        List<Post> posts = postMapper.searchByTag(tagName, 0, size);
        return new PageInfo<>(posts);
    }
    
    /**
     * 综合搜索
     */
    public PageInfo<Post> searchPosts(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<Post> posts = postMapper.searchPosts(keyword, 0, size);
        return new PageInfo<>(posts);
    }
    
    /**
     * 获取总数
     */
    public long getTotalCount() {
        return postMapper.countTotal();
    }
    
    /**
     * 获取作者文章数
     */
    public long getCountByAuthorId(Long authorId) {
        return postMapper.countByAuthorId(authorId);
    }
}
