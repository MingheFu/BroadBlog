package com.broadblog.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.broadblog.entity.Post;
import com.broadblog.entity.User;
import com.broadblog.repository.PostRepository;
import com.broadblog.repository.UserRepository;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // Create or update a post
    public Post savePost(Post post) {
        if (post.getAuthor() != null && post.getAuthor().getId() != null) {
            User author = userRepository.findById(post.getAuthor().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            post.setAuthor(author);
        } else {
            throw new RuntimeException("Author is required");
        }
        return postRepository.save(post);
    }

    // Get all posts
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // Get a post by ID
    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    // Delete a post by ID
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    // Custom: Get posts by author
    public List<Post> getPostsByAuthorId(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    public Page<Post> getPostsByPage(int page, int size) {
        // 验证分页参数
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        // 按创建时间倒序排列
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAll(pageable);
    }
    // 根据用户ID分页获取帖子
    public Page<Post> getPostsByAuthorIdWithPage(Long authorId, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByAuthorId(authorId, pageable);
    }
    
    // 搜索帖子（综合搜索：标题、内容、标签）
    public Page<Post> searchPosts(String keyword, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 如果关键词为空，返回所有帖子
        if (keyword == null || keyword.trim().isEmpty()) {
            return postRepository.findAll(pageable);
        }
        
        return postRepository.searchPosts(keyword.trim(), pageable);
    }
    
    // 按标题搜索
    public Page<Post> searchByTitle(String title, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
    
    // 按内容搜索
    public Page<Post> searchByContent(String content, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByContentContainingIgnoreCase(content, pageable);
    }
    
    // 按标签搜索
    public Page<Post> searchByTag(String tagName, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByTagsNameContaining(tagName, pageable);
    }

}