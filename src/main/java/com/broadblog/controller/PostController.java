package com.broadblog.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.dto.PostDTO;
import com.broadblog.entity.Post;
import com.broadblog.mapper.PostMapper;
import com.broadblog.security.CustomUserDetails;
import com.broadblog.service.PostService;
import com.broadblog.service.UserService;


@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, PostMapper postMapper, UserService userService) {
        this.postService = postService;
        this.postMapper = postMapper;
        this.userService = userService;
    }
    
    // 辅助方法：获取当前登录用户
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser().getId();
        }
        throw new RuntimeException("User not authenticated");
    }

    // 检查权限：用户只能管理自己的帖子，管理员可以管理所有帖子
    private boolean hasPermission(Long postAuthorId) {
        Long currentUserId = getCurrentUserId();
        // 如果是管理自己的帖子，允许
        if (currentUserId.equals(postAuthorId)) {
            return true;
        }
        // 如果是管理员，允许管理所有帖子
        return userService.isAdmin(currentUserId);
    }

    // Create a new post
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostDTO postDTO) {
        try {
            // 获取当前登录用户ID
            Long currentUserId = getCurrentUserId();
            System.out.println("Current user ID: " + currentUserId);
            
            // 强制设置作者为当前登录用户，忽略前端传入的 authorId
            postDTO.setAuthorId(currentUserId);
            
            // 创建帖子
            Post post = postMapper.toEntity(postDTO);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            
            System.out.println("Creating post: " + post.getTitle());
            Post savedPost = postService.savePost(post);
            return ResponseEntity.ok(postMapper.toDTO(savedPost));
        } catch (RuntimeException e) {
            System.err.println("Error creating post: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get all posts
    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        List<PostDTO> postDTOs = posts.stream()
            .map(postMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(postDTOs);
    }

    // Get a post by ID
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        Optional<Post> post = postService.getPostById(id);
        return post.map(p -> ResponseEntity.ok(postMapper.toDTO(p)))
                   .orElse(ResponseEntity.notFound().build());
    }

    // Update a post
    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id, @RequestBody PostDTO postDTO) {
        try {
            // 获取当前登录用户ID
            Long currentUserId = getCurrentUserId();
            
            Optional<Post> optionalPost = postService.getPostById(id);
            if (optionalPost.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Post post = optionalPost.get();
            
            // 权限验证：只能编辑自己的帖子，管理员可以编辑所有帖子
            if (!hasPermission(post.getAuthor().getId())) {
                return ResponseEntity.status(403).build(); // 403 Forbidden
            }
            
            // 更新帖子内容
            post.setTitle(postDTO.getTitle());
            post.setContent(postDTO.getContent());
            post.setUpdatedAt(LocalDateTime.now());
            
            Post updatedPost = postService.savePost(post);
            return ResponseEntity.ok(postMapper.toDTO(updatedPost));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete a post
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        try {
            // 获取当前登录用户ID
            Long currentUserId = getCurrentUserId();
            
            Optional<Post> optionalPost = postService.getPostById(id);
            if (optionalPost.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Post post = optionalPost.get();
            
            // 权限验证：只能删除自己的帖子，管理员可以删除所有帖子
            if (!hasPermission(post.getAuthor().getId())) {
                return ResponseEntity.status(403).build(); // 403 Forbidden
            }
            
            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 分页获取所有帖子
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getPostsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> pagePosts = postService.getPostsByPage(page, size);
        
        // 转换为 DTO
        List<PostDTO> postDTOs = pagePosts.getContent().stream()
            .map(postMapper::toDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", postDTOs);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalElements", pagePosts.getTotalElements());
        result.put("totalPages", pagePosts.getTotalPages());
        result.put("first", pagePosts.isFirst());
        result.put("last", pagePosts.isLast());
        
        return ResponseEntity.ok(result);
    }
    
    // 分页获取指定用户的帖子
    @GetMapping("/author/{authorId}/page")
    public ResponseEntity<Map<String, Object>> getPostsByAuthorWithPage(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> pagePosts = postService.getPostsByAuthorIdWithPage(authorId, page, size);
        
        // 转换为 DTO
        List<PostDTO> postDTOs = pagePosts.getContent().stream()
            .map(postMapper::toDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", postDTOs);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalElements", pagePosts.getTotalElements());
        result.put("totalPages", pagePosts.getTotalPages());
        result.put("first", pagePosts.isFirst());
        result.put("last", pagePosts.isLast());
        result.put("authorId", authorId);
        
        return ResponseEntity.ok(result);
    }
    
    // 获取当前用户的帖子（分页）
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Long currentUserId = getCurrentUserId();
            Page<Post> pagePosts = postService.getPostsByAuthorIdWithPage(currentUserId, page, size);
            
            // 转换为 DTO
            List<PostDTO> postDTOs = pagePosts.getContent().stream()
                .map(postMapper::toDTO)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", postDTOs);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("totalElements", pagePosts.getTotalElements());
            result.put("totalPages", pagePosts.getTotalPages());
            result.put("first", pagePosts.isFirst());
            result.put("last", pagePosts.isLast());
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 搜索帖子（综合搜索）
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> pagePosts = postService.searchPosts(keyword, page, size);
        
        // 转换为 DTO
        List<PostDTO> postDTOs = pagePosts.getContent().stream()
            .map(postMapper::toDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", postDTOs);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalElements", pagePosts.getTotalElements());
        result.put("totalPages", pagePosts.getTotalPages());
        result.put("first", pagePosts.isFirst());
        result.put("last", pagePosts.isLast());
        result.put("keyword", keyword);
        
        return ResponseEntity.ok(result);
    }
    
    // 按标题搜索
    @GetMapping("/search/title")
    public ResponseEntity<Map<String, Object>> searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> pagePosts = postService.searchByTitle(title, page, size);
        
        List<PostDTO> postDTOs = pagePosts.getContent().stream()
            .map(postMapper::toDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", postDTOs);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalElements", pagePosts.getTotalElements());
        result.put("totalPages", pagePosts.getTotalPages());
        result.put("searchType", "title");
        result.put("searchTerm", title);
        
        return ResponseEntity.ok(result);
    }
    
    // 按内容搜索
    @GetMapping("/search/content")
    public ResponseEntity<Map<String, Object>> searchByContent(
            @RequestParam String content,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> pagePosts = postService.searchByContent(content, page, size);
        
        List<PostDTO> postDTOs = pagePosts.getContent().stream()
            .map(postMapper::toDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", postDTOs);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalElements", pagePosts.getTotalElements());
        result.put("totalPages", pagePosts.getTotalPages());
        result.put("searchType", "content");
        result.put("searchTerm", content);
        
        return ResponseEntity.ok(result);
    }
    
    // 按标题或内容搜索（复合搜索）
    @GetMapping("/search/title-content")
    public ResponseEntity<Map<String, Object>> searchByTitleOrContent(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> pagePosts = postService.searchByTitleOrContent(keyword, page, size);
        
        List<PostDTO> postDTOs = pagePosts.getContent().stream()
            .map(postMapper::toDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", postDTOs);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalElements", pagePosts.getTotalElements());
        result.put("totalPages", pagePosts.getTotalPages());
        result.put("searchType", "title-content");
        result.put("searchTerm", keyword);
        
        return ResponseEntity.ok(result);
    }
    
    // 按标签搜索
    @GetMapping("/search/tag")
    public ResponseEntity<Map<String, Object>> searchByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> pagePosts = postService.searchByTag(tag, page, size);
        
        List<PostDTO> postDTOs = pagePosts.getContent().stream()
            .map(postMapper::toDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", postDTOs);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalElements", pagePosts.getTotalElements());
        result.put("totalPages", pagePosts.getTotalPages());
        result.put("searchType", "tag");
        result.put("searchTerm", tag);
        
        return ResponseEntity.ok(result);
    }

}