<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
      <el-button
        type="text"
        @click="showNotifications = true"
        class="notification-button"
      >
        <el-icon size="20">
          <Bell />
        </el-icon>
      </el-button>
    </el-badge>

    <!-- Notification Drawer -->
    <el-drawer
      v-model="showNotifications"
      title="Notifications"
      direction="rtl"
      size="400px"
    >
      <div class="notification-drawer">
        <div class="notification-header">
          <span>{{ unreadCount }} unread</span>
          <el-button
            v-if="unreadCount > 0"
            type="text"
            size="small"
            @click="markAllAsRead"
          >
            Mark all as read
          </el-button>
        </div>

        <el-divider />

        <div class="notification-list">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.isRead }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-content">
              <div class="notification-title">{{ notification.title }}</div>
              <div class="notification-text">{{ notification.content }}</div>
              <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
            </div>
            <div class="notification-actions">
              <el-button
                v-if="!notification.isRead"
                type="text"
                size="small"
                @click.stop="markAsRead(notification.id)"
              >
                Mark as read
              </el-button>
              <el-button
                type="text"
                size="small"
                @click.stop="deleteNotification(notification.id)"
              >
                Delete
              </el-button>
            </div>
          </div>

          <div v-if="notifications.length === 0" class="no-notifications">
            No notifications
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { notificationService, type Notification } from '@/services/notifications'
import { websocketService } from '@/services/websocket'

const userStore = useUserStore()

const showNotifications = ref(false)
const notifications = ref<Notification[]>([])
const unreadCount = ref(0)

const loadNotifications = async () => {
  if (!userStore.user) return
  
  try {
    const response = await notificationService.getUserNotifications(userStore.user.id.toString())
    notifications.value = response.notifications
    unreadCount.value = response.unreadCount
  } catch (error) {
    console.error('Failed to load notifications:', error)
  }
}

const loadUnreadNotifications = async () => {
  if (!userStore.user) return
  
  try {
    const unreadNotifications = await notificationService.getUnreadNotifications(userStore.user.id.toString())
    unreadCount.value = unreadNotifications.length
  } catch (error) {
    console.error('Failed to load unread notifications:', error)
  }
}

const markAsRead = async (id: number) => {
  try {
    await notificationService.markAsRead(id)
    await loadNotifications()
    await loadUnreadNotifications()
    ElMessage.success('Marked as read')
  } catch (error) {
    ElMessage.error('Failed to mark as read')
  }
}

const markAllAsRead = async () => {
  if (!userStore.user) return
  
  try {
    await notificationService.markAllAsRead(userStore.user.id.toString())
    await loadNotifications()
    await loadUnreadNotifications()
    ElMessage.success('All notifications marked as read')
  } catch (error) {
    ElMessage.error('Failed to mark all as read')
  }
}

const deleteNotification = async (id: number) => {
  try {
    await notificationService.deleteNotification(id)
    await loadNotifications()
    await loadUnreadNotifications()
    ElMessage.success('Notification deleted')
  } catch (error) {
    ElMessage.error('Failed to delete notification')
  }
}

const handleNotificationClick = (notification: Notification) => {
  if (!notification.isRead) {
    markAsRead(notification.id)
  }
  
  if (notification.targetUrl) {
    window.location.href = notification.targetUrl
  }
}

const formatTime = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return 'Just now'
  if (minutes < 60) return `${minutes}m ago`
  if (hours < 24) return `${hours}h ago`
  if (days < 7) return `${days}d ago`
  
  return date.toLocaleDateString()
}

// WebSocket event handlers
const handleNewNotification = (notification: Notification) => {
  notifications.value.unshift(notification)
  unreadCount.value++
  ElMessage.info(`New notification: ${notification.title}`)
}

onMounted(() => {
  loadNotifications()
  loadUnreadNotifications()
  
  // Setup WebSocket handlers
  websocketService.onNotification = handleNewNotification
})

onUnmounted(() => {
  websocketService.onNotification = null
})
</script>

<style scoped>
.notification-bell {
  position: relative;
}

.notification-badge {
  margin-right: 8px;
}

.notification-button {
  padding: 8px;
}

.notification-drawer {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  font-size: 14px;
  color: #666;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
}

.notification-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f5f5;
}

.notification-item.unread {
  background-color: #f0f8ff;
}

.notification-content {
  margin-bottom: 8px;
}

.notification-title {
  font-weight: 600;
  margin-bottom: 4px;
  color: #333;
}

.notification-text {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.no-notifications {
  text-align: center;
  color: #999;
  padding: 40px 0;
}
</style>
