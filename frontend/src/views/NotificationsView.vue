<template>
  <div class="notifications-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>Notifications</h2>
          <div class="header-actions">
            <el-button
              v-if="unreadCount > 0"
              type="primary"
              @click="markAllAsRead"
              :loading="markingAll"
            >
              Mark All as Read
            </el-button>
            <el-button
              type="danger"
              @click="deleteAllNotifications"
              :loading="deletingAll"
            >
              Delete All
            </el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <el-card>
          <div class="notifications-header">
            <div class="stats">
              <span class="stat-item">
                <strong>{{ totalCount }}</strong> total
              </span>
              <span class="stat-item">
                <strong>{{ unreadCount }}</strong> unread
              </span>
            </div>
            
            <el-radio-group v-model="filter" @change="handleFilterChange">
              <el-radio-button label="all">All</el-radio-button>
              <el-radio-button label="unread">Unread</el-radio-button>
            </el-radio-group>
          </div>

          <el-table
            :data="notifications"
            v-loading="loading"
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column prop="content" label="Content" min-width="300" />
            <el-table-column prop="type" label="Type" width="120" />
            <el-table-column prop="isRead" label="Status" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.isRead ? 'success' : 'warning'">
                  {{ scope.row.isRead ? 'Read' : 'Unread' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="Created At" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="200" fixed="right">
              <template #default="scope">
                <el-button
                  v-if="!scope.row.isRead"
                  size="small"
                  @click="markAsRead(scope.row.id)"
                >
                  Mark as Read
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click="deleteNotification(scope.row.id)"
                >
                  Delete
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="totalElements"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { notificationService, type Notification } from '@/services/notifications'
import { websocketService } from '@/services/websocket'

const userStore = useUserStore()

const loading = ref(false)
const markingAll = ref(false)
const deletingAll = ref(false)
const filter = ref('all')
const currentPage = ref(1)
const pageSize = ref(20)
const totalElements = ref(0)
const totalCount = ref(0)
const unreadCount = ref(0)

const notifications = ref<Notification[]>([])

const loadNotifications = async () => {
  if (!userStore.user) return
  
  try {
    loading.value = true
    const response = await notificationService.getUserNotifications(
      userStore.user.id.toString(),
      currentPage.value - 1,
      pageSize.value
    )
    
    if (filter.value === 'unread') {
      notifications.value = response.notifications.filter(n => !n.isRead)
    } else {
      notifications.value = response.notifications
    }
    
    totalElements.value = response.totalElements
    totalCount.value = response.totalElements
    unreadCount.value = response.unreadCount
  } catch (error: any) {
    ElMessage.error('Failed to load notifications')
    console.error('Load notifications error:', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadNotifications()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadNotifications()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadNotifications()
}

const markAsRead = async (id: number) => {
  try {
    await notificationService.markAsRead(id)
    ElMessage.success('Marked as read')
    await loadNotifications()
  } catch (error: any) {
    ElMessage.error('Failed to mark as read')
  }
}

const markAllAsRead = async () => {
  if (!userStore.user) return
  
  try {
    markingAll.value = true
    await notificationService.markAllAsRead(userStore.user.id.toString())
    ElMessage.success('All notifications marked as read')
    await loadNotifications()
  } catch (error: any) {
    ElMessage.error('Failed to mark all as read')
  } finally {
    markingAll.value = false
  }
}

const deleteNotification = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to delete this notification?',
      'Confirm Delete',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    await notificationService.deleteNotification(id)
    ElMessage.success('Notification deleted')
    await loadNotifications()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to delete notification')
    }
  }
}

const deleteAllNotifications = async () => {
  if (!userStore.user) return
  
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to delete all notifications? This action cannot be undone.',
      'Confirm Delete All',
      {
        confirmButtonText: 'Delete All',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    deletingAll.value = true
    await notificationService.deleteAllUserNotifications(userStore.user.id.toString())
    ElMessage.success('All notifications deleted')
    await loadNotifications()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to delete all notifications')
    }
  } finally {
    deletingAll.value = false
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('en-US')
}

// WebSocket event handlers
const handleNewNotification = (notification: Notification) => {
  if (notification.recipientId === userStore.user?.id.toString()) {
    notifications.value.unshift(notification)
    totalCount.value++
    unreadCount.value++
    ElMessage.info(`New notification: ${notification.title}`)
  }
}

onMounted(() => {
  loadNotifications()
  
  // Setup WebSocket handlers
  websocketService.onNotification = handleNewNotification
})

onUnmounted(() => {
  websocketService.onNotification = null
})
</script>

<style scoped>
.notifications-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.header-content h2 {
  margin: 0;
  color: #409eff;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.notifications-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  font-size: 14px;
  color: #666;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
