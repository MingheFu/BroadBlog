package com.broadblog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.broadblog.entity.NotificationMessage;

@Repository
public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, Long> {
    
    /**
     * 根据接收者ID查找通知
     */
    List<NotificationMessage> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
    
    /**
     * 根据接收者ID和类型查找通知
     */
    List<NotificationMessage> findByRecipientIdAndTypeOrderByCreatedAtDesc(String recipientId, NotificationMessage.NotificationType type);
    
    /**
     * 查找未读通知
     */
    List<NotificationMessage> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(String recipientId);
    
    /**
     * 统计未读通知数量
     */
    long countByRecipientIdAndIsReadFalse(String recipientId);
    
    /**
     * 根据发送者ID查找通知
     */
    List<NotificationMessage> findBySenderIdOrderByCreatedAtDesc(String senderId);
    
    /**
     * 根据类型查找通知
     */
    List<NotificationMessage> findByTypeOrderByCreatedAtDesc(NotificationMessage.NotificationType type);
    
    /**
     * 标记通知为已读
     */
    @Modifying
    @Query("UPDATE NotificationMessage n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);
    
    /**
     * 批量标记通知为已读
     */
    @Modifying
    @Query("UPDATE NotificationMessage n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipientId = :recipientId AND n.isRead = false")
    void markAllAsRead(@Param("recipientId") String recipientId);
    
    /**
     * 删除旧通知
     */
    @Modifying
    @Query("DELETE FROM NotificationMessage n WHERE n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
