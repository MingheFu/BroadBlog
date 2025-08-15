package com.broadblog.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.entity.CommentMessage;
import com.broadblog.entity.NotificationMessage;

@RestController
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 处理新评论消息
     * 客户端发送到 /app/comment 的消息会被广播到 /topic/comments
     */
    @MessageMapping("/comment")
    @SendTo("/topic/comments")
    public CommentMessage handleComment(@Payload CommentMessage commentMessage) {
        // 这里可以添加业务逻辑，比如保存到数据库
        return commentMessage;
    }

    /**
     * 处理评论回复
     * 发送到特定帖子的评论频道
     */
    @MessageMapping("/comment/reply")
    public void handleCommentReply(@Payload CommentMessage replyMessage) {
        // 发送到特定帖子的评论频道
        messagingTemplate.convertAndSend("/topic/post/" + replyMessage.getPostId() + "/comments", replyMessage);
        
        // 如果是回复其他评论，发送通知给被回复的用户
        // 这里需要根据业务逻辑获取被回复用户的ID
        // messagingTemplate.convertAndSendToUser(recipientId, "/queue/notifications", notification);
    }

    /**
     * 处理评论点赞
     */
    @MessageMapping("/comment/like")
    public void handleCommentLike(@Payload CommentMessage likeMessage) {
        // 发送到特定帖子的评论频道
        messagingTemplate.convertAndSend("/topic/post/" + likeMessage.getPostId() + "/comments", likeMessage);
    }

    /**
     * 处理用户加入聊天
     */
    @MessageMapping("/chat.join")
    @SendTo("/topic/public")
    public CommentMessage addUser(@Payload CommentMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // 将用户名添加到 WebSocket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getAuthorName());
        return chatMessage;
    }

    /**
     * 发送个人通知
     * 这个方法可以被其他服务调用，用于发送个人通知
     */
    public void sendPersonalNotification(String userId, NotificationMessage notification) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    /**
     * 发送广播通知
     * 这个方法可以被其他服务调用，用于发送系统广播
     */
    public void sendBroadcastNotification(NotificationMessage notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * 发送帖子相关的通知
     * 这个方法可以被其他服务调用，用于发送帖子相关的通知
     */
    public void sendPostNotification(String postId, NotificationMessage notification) {
        messagingTemplate.convertAndSend("/topic/post/" + postId + "/notifications", notification);
    }
}
