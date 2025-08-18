package com.broadblog.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.entity.NotificationMessage;
import com.broadblog.repository.NotificationMessageRepository;
import com.broadblog.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final NotificationService notificationService;
    private final NotificationMessageRepository notificationMessageRepository;

    public NotificationApiController(NotificationService notificationService, 
                                   NotificationMessageRepository notificationMessageRepository) {
        this.notificationService = notificationService;
        this.notificationMessageRepository = notificationMessageRepository;
    }

    /**
     * 获取用户的通知列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<NotificationMessage> notifications = notificationMessageRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId);
            
            // 简单的分页实现
            int start = page * size;
            int end = Math.min(start + size, notifications.size());
            List<NotificationMessage> pageNotifications = notifications.subList(start, end);
            
            // 统计未读数量
            long unreadCount = notificationMessageRepository.countByRecipientIdAndIsReadFalse(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", pageNotifications);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalElements", notifications.size());
            response.put("totalPages", (int) Math.ceil((double) notifications.size() / size));
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "failed to retrieve notifications: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取用户的未读通知
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationMessage>> getUnreadNotifications(@PathVariable String userId) {
        try {
            List<NotificationMessage> unreadNotifications = notificationMessageRepository
                .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
            return ResponseEntity.ok(unreadNotifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        try {
            notificationMessageRepository.markAsRead(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "failed to mark notification as read: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 批量标记用户的所有通知为已读
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable String userId) {
        try {
            notificationMessageRepository.markAllAsRead(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "All notifications marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "failed to mark all notifications as read: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 删除单个通知
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        try {
            notificationMessageRepository.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "failed to delete notification: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 删除用户的所有通知
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteAllUserNotifications(@PathVariable String userId) {
        try {
            List<NotificationMessage> userNotifications = notificationMessageRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId);
            notificationMessageRepository.deleteAll(userNotifications);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "All notifications deleted");
            response.put("deletedCount", String.valueOf(userNotifications.size()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "failed to delete all notifications: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
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
            response.put("message", "Notification sent successfully");
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "failed to send notification: " + e.getMessage());
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
            response.put("message", "Broadcast notification sent successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "failed to send broadcast notification: " + e.getMessage());
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
            response.put("message", "Follow notification sent successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "failed to send follow notification: " + e.getMessage());
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
            response.put("message", "Mention notification sent successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "failed to send mention notification: " + e.getMessage());
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
            response.put("message", "Like notification sent successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "failed to send like notification: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
