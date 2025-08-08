package com.broadblog.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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
import com.broadblog.entity.User;
import com.broadblog.mapper.PostMapper;
import com.broadblog.service.MyBatisPostService;
import com.broadblog.service.UserService;
import com.github.pagehelper.PageInfo;

@RestController
@RequestMapping("/api/mybatis/posts")
public class MyBatisPostController {
    
    @Autowired
    @Qualifier("myBatisPostService")
    private MyBatisPostService postService;
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取所有文章（分页）
     */
    @GetMapping("/page")
    public ResponseEntity<?> getPostsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageInfo<Post> pageInfo = postService.getPostsByPage(page, size);
            Map<String, Object> result = new HashMap<>();
            result.put("content", pageInfo.getList());
            result.put("totalElements", pageInfo.getTotal());
            result.put("totalPages", pageInfo.getPages());
            result.put("currentPage", pageInfo.getPageNum());
            result.put("pageSize", pageInfo.getPageSize());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Failed to get posts: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 获取我的文章（分页）
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = getCurrentUserId();
            PageInfo<Post> pageInfo = postService.getPostsByAuthorIdWithPage(currentUserId, page, size);
            Map<String, Object> result = new HashMap<>();
            result.put("content", pageInfo.getList());
            result.put("totalElements", pageInfo.getTotal());
            result.put("totalPages", pageInfo.getPages());
            result.put("currentPage", pageInfo.getPageNum());
            result.put("pageSize", pageInfo.getPageSize());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Failed to get my posts: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 根据ID获取文章
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        try {
            return postService.getPostById(id)
                .map(post -> ResponseEntity.ok(postMapper.toDTO(post)))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Failed to get post: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 创建文章
     */
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostDTO postDTO) {
        try {
            Long currentUserId = getCurrentUserId();
            User currentUser = userService.getUserById(currentUserId).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"User not found\"}");
            }
            
            Post post = postMapper.toEntity(postDTO);
            post.setAuthor(currentUser);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            
            Post savedPost = postService.savePost(post);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(postMapper.toDTO(savedPost));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"Failed to create post: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody PostDTO postDTO) {
        try {
            Long currentUserId = getCurrentUserId();
            
            return postService.getPostById(id)
                .map(post -> {
                    // 权限检查：只有作者或管理员可以编辑
                    if (!hasPermission(post.getAuthor().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\": \"Access denied\"}");
                    }
                    
                    post.setTitle(postDTO.getTitle());
                    post.setContent(postDTO.getContent());
                    post.setUpdatedAt(LocalDateTime.now());
                    
                    Post updatedPost = postService.savePost(post);
                    return ResponseEntity.ok(postMapper.toDTO(updatedPost));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"Failed to update post: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            
            return postService.getPostById(id)
                .map(post -> {
                    // 权限检查：只有作者或管理员可以删除
                    if (!hasPermission(post.getAuthor().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\": \"Access denied\"}");
                    }
                    
                    postService.deletePost(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Failed to delete post: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 搜索文章
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageInfo<Post> pageInfo = postService.searchPosts(keyword, page, size);
            Map<String, Object> result = new HashMap<>();
            result.put("content", pageInfo.getList());
            result.put("totalElements", pageInfo.getTotal());
            result.put("totalPages", pageInfo.getPages());
            result.put("currentPage", pageInfo.getPageNum());
            result.put("pageSize", pageInfo.getPageSize());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Failed to search posts: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 性能测试接口
     */
    @GetMapping("/performance-test")
    public ResponseEntity<?> performanceTest() {
        try {
            long startTime = System.currentTimeMillis();
            
            // 测试查询性能
            PageInfo<Post> pageInfo = postService.getPostsByPage(1, 100);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Map<String, Object> result = new HashMap<>();
            result.put("queryTime", duration + "ms");
            result.put("totalRecords", pageInfo.getTotal());
            result.put("returnedRecords", pageInfo.getList().size());
            result.put("framework", "MyBatis");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Performance test failed: " + e.getMessage() + "\"}");
        }
    }
    
    // 辅助方法
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.broadblog.security.CustomUserDetails) {
            com.broadblog.security.CustomUserDetails userDetails = 
                (com.broadblog.security.CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser().getId();
        }
        throw new RuntimeException("User not authenticated");
    }
    
    private boolean hasPermission(Long authorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.broadblog.security.CustomUserDetails) {
            com.broadblog.security.CustomUserDetails userDetails = 
                (com.broadblog.security.CustomUserDetails) authentication.getPrincipal();
            Long currentUserId = userDetails.getUser().getId();
            return currentUserId.equals(authorId) || userService.isAdmin(currentUserId);
        }
        return false;
    }
}
