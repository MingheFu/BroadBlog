package com.broadblog.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.dto.PostDTO;
import com.broadblog.entity.Post;
import com.broadblog.entity.Tag;
import com.broadblog.mapper.PostMapper;
import com.broadblog.service.CacheService;
import com.broadblog.service.CacheWarmUpService;
import com.broadblog.service.PostService;
import com.broadblog.service.TagService;
import com.broadblog.service.UserService;

/**
 * 缓存管理控制器
 * 提供缓存预热、清除、状态查看等功能
 */
@RestController
@RequestMapping("/api/cache")
@PreAuthorize("hasRole('ADMIN')") // 只有管理员可以访问缓存管理功能
public class CacheController {

    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private CacheWarmUpService cacheWarmUpService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PostMapper postMapper;

    /**
     * 获取缓存状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 检查各种缓存键是否存在
        String[] cacheKeys = {
            "hot_posts",
            "popular_tags_ranking", 
            "stats:total_users",
            "stats:total_admins"
        };
        
        Map<String, Boolean> keyStatus = new HashMap<>();
        for (String key : cacheKeys) {
            keyStatus.put(key, cacheService.exists(key));
        }
        
        status.put("cacheKeys", keyStatus);
        status.put("message", "cache status query success");
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * 手动预热缓存
     */
    @PostMapping("/warmup")
    public ResponseEntity<Map<String, String>> warmUpCache() {
        try {
            cacheWarmUpService.manualWarmUp();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "warm up cache done");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "warm up cache error: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 清除所有缓存
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearAllCache() {
        try {
            cacheWarmUpService.clearAllCache();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "all cache cleared");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "clear cache error: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取热门文章排行
     */
    @GetMapping("/hot-posts")
    public ResponseEntity<List<PostDTO>> getHotPosts(@RequestParam(defaultValue = "10") int top) {
        try {
            List<Post> hotPosts = postService.getHotPosts(top);
            List<PostDTO> hotPostDTOs = hotPosts.stream()
                .map(postMapper::toDTO)
                .toList();
            
            return ResponseEntity.ok(hotPostDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取热门标签排行
     */
    @GetMapping("/popular-tags")
    public ResponseEntity<List<Tag>> getPopularTags(@RequestParam(defaultValue = "20") int top) {
        try {
            List<Tag> popularTags = tagService.getTopPopularTags(top);
            return ResponseEntity.ok(popularTags);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取文章阅读量
     */
    @GetMapping("/post-views/{postId}")
    public ResponseEntity<Map<String, Object>> getPostViews(@RequestParam Long postId) {
        try {
            Long views = postService.getPostViews(postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("postId", postId);
            response.put("views", views);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取用户统计信息（从缓存）
     */
    @GetMapping("/user-stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> stats = userService.getUserStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取在线用户数量
     */
    @GetMapping("/online-users")
    public ResponseEntity<Map<String, Object>> getOnlineUserCount() {
        try {
            Long onlineCount = userService.getOnlineUserCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("onlineUsers", onlineCount);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取标签使用次数
     */
    @GetMapping("/tag-usage/{tagId}")
    public ResponseEntity<Map<String, Object>> getTagUsage(@RequestParam Long tagId) {
        try {
            Long usage = tagService.getTagUsageCount(tagId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("tagId", tagId);
            response.put("usageCount", usage);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 清除指定前缀的缓存
     */
    @DeleteMapping("/clear-prefix")
    public ResponseEntity<Map<String, String>> clearCacheByPrefix(@RequestParam String prefix) {
        try {
            cacheService.clearByPrefix(prefix);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "clear cache by prefix: " + prefix + " done");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "clear cache by prefix: " + prefix + " error: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
