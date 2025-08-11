package com.broadblog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 缓存预热服务
 * 在应用启动完成后，自动预热各种缓存数据
 */
@Service
public class CacheWarmUpService {

    @Autowired
    private PostService postService;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CacheService cacheService;

    /**
     * 应用启动完成后，自动预热缓存
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCacheOnStartup() {
        System.out.println("application started, warm up cache");
        
        try {
            // 预热文章缓存
            warmUpPostCache();
            
            // 预热标签缓存
            warmUpTagCache();
            
            // 预热用户缓存
            warmUpUserCache();
            
            // 预热系统统计缓存
            warmUpSystemStatsCache();
            
            System.out.println("warm up cache done");
            
        } catch (Exception e) {
            System.err.println("warm up cache error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 预热文章相关缓存
     */
    private void warmUpPostCache() {
        System.out.println("warm up post cache");
        
        try {
            // 预热第一页文章
            postService.getPostsByPage(1, 10);
            
            // 预热热门文章排行
            postService.getHotPosts(10);
            
            System.out.println("warm up post cache done");
        } catch (Exception e) {
            System.err.println("warm up post cache error: " + e.getMessage());
        }
    }
    
    /**
     * 预热标签相关缓存
     */
    private void warmUpTagCache() {
        System.out.println("warm up tag cache");
        
        try {
            tagService.warmUpTagCache();
        } catch (Exception e) {
            System.err.println("warm up tag cache error: " + e.getMessage());
        }
    }
    
    /**
     * 预热用户相关缓存
     */
    private void warmUpUserCache() {
        System.out.println("warm up user cache");
        
        try {
            userService.warmUpUserCache();
        } catch (Exception e) {
            System.err.println("warm up user cache error: " + e.getMessage());
        }
    }
    
    /**
     * 预热系统统计缓存
     */
    private void warmUpSystemStatsCache() {
        System.out.println("warm up system stats cache");
        
        try {
            // 预热用户统计
            userService.getUserStats();
            
            // 预热标签统计
            tagService.getTagStatistics();
            
            System.out.println("warm up system stats cache done");
        } catch (Exception e) {
            System.err.println("warm up system stats cache error: " + e.getMessage());
        }
    }
    
    /**
     * 手动预热缓存（可以在需要时调用）
     */
    public void manualWarmUp() {
        System.out.println("manual warm up cache");
        warmUpPostCache();
        warmUpTagCache();
        warmUpUserCache();
        warmUpSystemStatsCache();
        System.out.println("manual warm up cache done");
    }
    
    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        System.out.println("clear all cache");
        cacheService.clearAll();
        System.out.println("all cache cleared");
    }
    
    /**
     * 获取缓存状态信息
     */
    public void getCacheStatus() {
        System.out.println("cache status");
        
        // 检查各种缓存键是否存在
        String[] cacheKeys = {
            "hot_posts",
            "popular_tags_ranking", 
            "stats:total_users",
            "stats:total_admins"
        };
        
        for (String key : cacheKeys) {
            boolean exists = cacheService.exists(key);
            System.out.println("  " + key + ": " + (exists ? "exists" : "not exists"));
        }
    }
}
