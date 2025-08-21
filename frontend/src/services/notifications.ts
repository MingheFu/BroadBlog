import api from './api'

export interface Notification {
  id: number
  title: string
  content: string
  type: string
  recipientId: string
  senderId?: string
  isRead: boolean
  createdAt: string
  readAt?: string
  targetUrl?: string
}

export interface NotificationResponse {
  notifications: Notification[]
  currentPage: number
  pageSize: number
  totalElements: number
  totalPages: number
  unreadCount: number
}

export interface NotificationForm {
  userId?: string
  title: string
  content: string
  targetUrl?: string
}

export const notificationService = {
  // 获取用户通知列表
  async getUserNotifications(userId: string, page: number = 0, size: number = 20): Promise<NotificationResponse> {
    const response = await api.get(`/notifications/user/${userId}?page=${page}&size=${size}`)
    return response.data
  },

  // 获取用户未读通知
  async getUnreadNotifications(userId: string): Promise<Notification[]> {
    const response = await api.get(`/notifications/user/${userId}/unread`)
    return response.data
  },

  // 标记通知为已读
  async markAsRead(id: number): Promise<void> {
    await api.put(`/notifications/${id}/read`)
  },

  // 批量标记所有通知为已读
  async markAllAsRead(userId: string): Promise<void> {
    await api.put(`/notifications/user/${userId}/read-all`)
  },

  // 删除单个通知
  async deleteNotification(id: number): Promise<void> {
    await api.delete(`/notifications/${id}`)
  },

  // 删除用户所有通知
  async deleteAllUserNotifications(userId: string): Promise<void> {
    await api.delete(`/notifications/user/${userId}`)
  },

  // 发送个人通知
  async sendPersonalNotification(data: NotificationForm): Promise<void> {
    await api.post('/notifications/personal', data)
  },

  // 发送广播通知
  async sendBroadcastNotification(data: Omit<NotificationForm, 'userId'>): Promise<void> {
    await api.post('/notifications/broadcast', data)
  },

  // 发送关注通知
  async sendFollowNotification(data: {
    followedUserId: string
    followerId: string
    followerName: string
    followerAvatar?: string
  }): Promise<void> {
    await api.post('/notifications/follow', data)
  },

  // 发送提及通知
  async sendMentionNotification(data: {
    mentionedUserId: string
    mentionerId: string
    mentionerName: string
    mentionerAvatar?: string
    postId: string
    postTitle: string
  }): Promise<void> {
    await api.post('/notifications/mention', data)
  },

  // 发送点赞通知
  async sendLikeNotification(data: {
    targetUserId: string
    likerId: string
    likerName: string
    likerAvatar?: string
    targetType: string
    targetId: string
    targetTitle?: string
  }): Promise<void> {
    await api.post('/notifications/like', data)
  }
}
