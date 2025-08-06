package com.broadblog.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.broadblog.entity.Post;
import com.broadblog.entity.Tag;
import com.broadblog.entity.User;
import com.broadblog.repository.PostRepository;
import com.broadblog.repository.UserRepository;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagService tagService;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository, TagService tagService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagService = tagService;
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
        
        // 处理标签使用计数
        Set<Tag> oldTags = new HashSet<>();
        if (post.getId() != null) {
            // 更新帖子：获取原有标签
            Optional<Post> existingPost = postRepository.findById(post.getId());
            if (existingPost.isPresent() && existingPost.get().getTags() != null) {
                oldTags = new HashSet<>(existingPost.get().getTags());
            }
        }
        
        Post savedPost = postRepository.save(post);
        
        // 更新标签使用计数
        updateTagUsageCounts(oldTags, savedPost.getTags() != null ? new HashSet<>(savedPost.getTags()) : new HashSet<>());
        
        return savedPost;
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
        // 获取要删除的帖子的标签，减少使用计数
        Optional<Post> postToDelete = postRepository.findById(id);
        if (postToDelete.isPresent() && postToDelete.get().getTags() != null) {
            for (Tag tag : postToDelete.get().getTags()) {
                tagService.decrementTagUsage(tag.getId());
            }
        }
        
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
    
    // 按标题或内容搜索（复合搜索）
    public Page<Post> searchByTitleOrContent(String keyword, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByTitleOrContentContaining(keyword, pageable);
    }
    
    // 按标签搜索
    public Page<Post> searchByTag(String tagName, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByTagsNameContaining(tagName, pageable);
    }
    
    // 辅助方法：更新标签使用计数
    private void updateTagUsageCounts(Set<Tag> oldTags, Set<Tag> newTags) {
        // 找出被移除的标签，减少使用计数
        for (Tag oldTag : oldTags) {
            if (!newTags.contains(oldTag)) {
                tagService.decrementTagUsage(oldTag.getId());
            }
        }
        
        // 找出新添加的标签，增加使用计数
        for (Tag newTag : newTags) {
            if (!oldTags.contains(newTag)) {
                tagService.incrementTagUsage(newTag.getId());
            }
        }
    }

}