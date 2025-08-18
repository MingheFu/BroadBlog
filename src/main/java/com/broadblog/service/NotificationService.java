package com.broadblog.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.broadblog.entity.Comment;
import com.broadblog.entity.NotificationMessage;

@Service
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 发送新评论通知
     */
    public void sendNewCommentNotification(String postAuthorId, Comment comment) {
        if (!postAuthorId.equals(comment.getAuthor().getId().toString())) {
            NotificationMessage notification = new NotificationMessage(
                "new comment",
                comment.getAuthor().getUsername() + " comment your post" + comment.getPost().getTitle(),
                postAuthorId,
                comment.getAuthor().getId().toString(),
                comment.getAuthor().getUsername(),
                comment.getAuthor().getAvatar(),
                "/post/" + comment.getPost().getId(),
                NotificationMessage.NotificationType.POST_COMMENT
            );
            // 让 JPA 自动生成 ID
            sendPersonalNotification(postAuthorId, notification);
        }
    }

    /**
     * 发送评论回复通知
     */
    public void sendCommentReplyNotification(String commentAuthorId, Comment reply) {
        if (!commentAuthorId.equals(reply.getAuthor().getId().toString())) {
            NotificationMessage notification = new NotificationMessage(
                "comment reply",
                reply.getAuthor().getUsername() + " reply your comment",
                commentAuthorId,
                reply.getAuthor().getId().toString(),
                reply.getAuthor().getUsername(),
                reply.getAuthor().getAvatar(),
                "/post/" + reply.getPost().getId(),
                NotificationMessage.NotificationType.COMMENT_REPLY
            );
            // 让 JPA 自动生成 ID
            sendPersonalNotification(commentAuthorId, notification);
        }
    }

    /**
     * 发送点赞通知
     */
    public void sendLikeNotification(String targetUserId, String likerId, String likerName, 
                                   String likerAvatar, String targetType, String targetId, String targetTitle) {
        if (!targetUserId.equals(likerId)) {
            NotificationMessage notification = new NotificationMessage(
                "new like",
                likerName + " like your " + targetType,
                targetUserId,
                likerId,
                likerName,
                likerAvatar,
                "/post/" + targetId,
                NotificationMessage.NotificationType.POST_LIKE
            );
            // 让 JPA 自动生成 ID
            sendPersonalNotification(targetUserId, notification);
        }
    }

    /**
     * 发送新关注者通知
     */
    public void sendNewFollowerNotification(String followedUserId, String followerId, 
                                          String followerName, String followerAvatar) {
        if (!followedUserId.equals(followerId)) {
            NotificationMessage notification = new NotificationMessage(
                "new follower",
                followerName + " follow you",
                followedUserId,
                followerId,
                followerName,
                followerAvatar,
                "/user/" + followerId,
                NotificationMessage.NotificationType.NEW_FOLLOWER
            );
            // 让 JPA 自动生成 ID
            sendPersonalNotification(followedUserId, notification);
        }
    }

    /**
     * 发送提及通知
     */
    public void sendMentionNotification(String mentionedUserId, String mentionerId, 
                                      String mentionerName, String mentionerAvatar, 
                                      String postId, String postTitle) {
        if (!mentionedUserId.equals(mentionerId)) {
            NotificationMessage notification = new NotificationMessage(
                "mention notification",
                mentionerName + " mention you in post《" + postTitle + "》",
                mentionedUserId,
                mentionerId,
                mentionerName,
                mentionerAvatar,
                "/post/" + postId,
                NotificationMessage.NotificationType.MENTION
            );
            // 让 JPA 自动生成 ID
            sendPersonalNotification(mentionedUserId, notification);
        }
    }

    /**
     * 发送系统通知
     */
    public void sendSystemNotification(String userId, String title, String content, String targetUrl) {
        NotificationMessage notification = new NotificationMessage(
            title,
            content,
            userId,
            "system",
            "system",
            "/images/system-avatar.png",
            targetUrl,
            NotificationMessage.NotificationType.SYSTEM_NOTIFICATION
                    );
            // 让 JPA 自动生成 ID
            sendPersonalNotification(userId, notification);
    }

    /**
     * 发送广播通知
     */
    public void sendBroadcastNotification(String title, String content, String targetUrl) {
        NotificationMessage notification = new NotificationMessage(
            title,
            content,
            "all",
            "system",
            "system",
            "/images/system-avatar.png",
            targetUrl,
            NotificationMessage.NotificationType.SYSTEM_NOTIFICATION
        );
        // 让 JPA 自动生成 ID
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * 发送个人通知
     */
    private void sendPersonalNotification(String userId, NotificationMessage notification) {
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/notifications",
            notification
        );
    }
}
