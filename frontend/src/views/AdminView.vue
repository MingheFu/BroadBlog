<template>
  <div class="admin-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>Admin Panel</h2>
        </div>
      </el-header>
      
      <el-main>
        <!-- Admin Statistics -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ adminStats.totalPosts }}</div>
                  <div class="stat-label">Total Posts</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ adminStats.totalUsers }}</div>
                  <div class="stat-label">Total Users</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ adminStats.totalComments }}</div>
                  <div class="stat-label">Total Comments</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon><Collection /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ adminStats.totalTags }}</div>
                  <div class="stat-label">Total Tags</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- Admin Actions -->
        <el-row :gutter="20" class="actions-row">
          <el-col :span="8">
            <el-card class="action-card">
              <template #header>
                <div class="card-header">
                  <span>User Management</span>
                </div>
              </template>
              <div class="card-content">
                <p>Manage all users in the system</p>
                <el-button type="primary" @click="$router.push('/users')">
                  Manage Users
                </el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="action-card">
              <template #header>
                <div class="card-header">
                  <span>Content Management</span>
                </div>
              </template>
              <div class="card-content">
                <p>Manage all posts and comments</p>
                <el-button type="primary" @click="$router.push('/posts')">
                  Manage Posts
                </el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="action-card">
              <template #header>
                <div class="card-header">
                  <span>Tag Management</span>
                </div>
              </template>
              <div class="card-content">
                <p>Manage tags and categories</p>
                <el-button type="primary" @click="$router.push('/tags')">
                  Manage Tags
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- System Information -->
        <el-card class="system-info">
          <template #header>
            <div class="card-header">
              <span>System Information</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="System Version">BroadBlog v1.0.0</el-descriptions-item>
            <el-descriptions-item label="Database">MySQL</el-descriptions-item>
            <el-descriptions-item label="Backend">Spring Boot</el-descriptions-item>
            <el-descriptions-item label="Frontend">Vue.js 3</el-descriptions-item>
            <el-descriptions-item label="Current User">{{ userStore.user?.username }}</el-descriptions-item>
            <el-descriptions-item label="User Role">Administrator</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { statsService, type SystemStats } from '@/services/stats'

const userStore = useUserStore()

const loading = ref(false)

const adminStats = reactive<SystemStats>({
  totalPosts: 0,
  totalUsers: 0,
  totalComments: 0,
  totalTags: 0
})

const loadAdminStats = async () => {
  try {
    loading.value = true
    const stats = await statsService.getSystemStats()
    Object.assign(adminStats, stats)
  } catch (error: any) {
    console.error('Failed to load admin stats:', error)
    ElMessage.error('Failed to load system statistics')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAdminStats()
})
</script>

<style scoped>
.admin-container {
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

.stats-row {
  margin-bottom: 20px;
}

.actions-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.action-card {
  height: 200px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  font-size: 48px;
  color: #409eff;
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: center;
  padding: 20px 0;
}

.card-content p {
  margin-bottom: 20px;
  color: #606266;
}

.system-info {
  margin-top: 20px;
}
</style>
