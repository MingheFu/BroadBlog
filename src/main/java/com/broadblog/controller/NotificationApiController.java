package com.broadblog.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final NotificationService notificationService;

    public NotificationApiController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 发送个人通知
     */
    @PostMapping("/personal")
    public ResponseEntity<Map<String, String>> sendPersonalNotification(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String title = request.get("title");
            String content = request.get("content");
            String targetUrl = request.get("targetUrl");

            if (userId == null || title == null || content == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少必要参数"));
            }

            notificationService.sendSystemNotification(userId, title, content, targetUrl != null ? targetUrl : "/");
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "通知发送成功");
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "发送通知失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 发送广播通知
     */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, String>> sendBroadcastNotification(@RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String content = request.get("content");
            String targetUrl = request.get("targetUrl");

            if (title == null || content == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少必要参数"));
            }

            notificationService.sendBroadcastNotification(title, content, targetUrl != null ? targetUrl : "/");
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "广播通知发送成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "发送广播通知失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 发送新关注者通知
     */
    @PostMapping("/follow")
    public ResponseEntity<Map<String, String>> sendFollowNotification(@RequestBody Map<String, String> request) {
        try {
            String followedUserId = request.get("followedUserId");
            String followerId = request.get("followerId");
            String followerName = request.get("followerName");
            String followerAvatar = request.get("followerAvatar");

            if (followedUserId == null || followerId == null || followerName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少必要参数"));
            }

            notificationService.sendNewFollowerNotification(
                followedUserId, 
                followerId, 
                followerName, 
                followerAvatar != null ? followerAvatar : "/images/default-avatar.png"
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "关注通知发送成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "发送关注通知失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 发送提及通知
     */
    @PostMapping("/mention")
    public ResponseEntity<Map<String, String>> sendMentionNotification(@RequestBody Map<String, String> request) {
        try {
            String mentionedUserId = request.get("mentionedUserId");
            String mentionerId = request.get("mentionerId");
            String mentionerName = request.get("mentionerName");
            String mentionerAvatar = request.get("mentionerAvatar");
            String postId = request.get("postId");
            String postTitle = request.get("postTitle");

            if (mentionedUserId == null || mentionerId == null || mentionerName == null || postId == null || postTitle == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少必要参数"));
            }

            notificationService.sendMentionNotification(
                mentionedUserId, 
                mentionerId, 
                mentionerName, 
                mentionerAvatar != null ? mentionerAvatar : "/images/default-avatar.png",
                postId, 
                postTitle
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "提及通知发送成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "发送提及通知失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 发送点赞通知
     */
    @PostMapping("/like")
    public ResponseEntity<Map<String, String>> sendLikeNotification(@RequestBody Map<String, String> request) {
        try {
            String targetUserId = request.get("targetUserId");
            String likerId = request.get("likerId");
            String likerName = request.get("likerName");
            String likerAvatar = request.get("likerAvatar");
            String targetType = request.get("targetType");
            String targetId = request.get("targetId");
            String targetTitle = request.get("targetTitle");

            if (targetUserId == null || likerId == null || likerName == null || targetType == null || targetId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少必要参数"));
            }

            notificationService.sendLikeNotification(
                targetUserId, 
                likerId, 
                likerName, 
                likerAvatar != null ? likerAvatar : "/images/default-avatar.png",
                targetType, 
                targetId, 
                targetTitle != null ? targetTitle : "内容"
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "点赞通知发送成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "发送点赞通知失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
