<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { statsService, type SystemStats } from '@/services/stats'

const router = useRouter()
const userStore = useUserStore()

const activeIndex = ref('home')
const loading = ref(false)

const stats = reactive<SystemStats>({
  totalPosts: 0,
  totalUsers: 0,
  totalComments: 0,
  totalTags: 0
})

const handleSelect = (key: string) => {
  switch (key) {
    case 'posts':
      router.push('/posts')
      break
    case 'users':
      if (userStore.isAdmin()) {
        router.push('/users')
      } else {
        ElMessage.warning('Access denied. Admin role required.')
      }
      break
    case 'comments':
      router.push('/comments')
      break
    case 'tags':
      router.push('/tags')
      break
    case 'admin':
      if (userStore.isAdmin()) {
        router.push('/admin')
      } else {
        ElMessage.warning('Access denied. Admin role required.')
      }
      break
  }
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('Profile feature is under development...')
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      ElMessage.success('Logged out successfully')
      break
  }
}

const loadStats = async () => {
  try {
    loading.value = true
    if (userStore.isAdmin()) {
      // Admin can see all stats
      const systemStats = await statsService.getSystemStats()
      Object.assign(stats, systemStats)
    } else {
      // Regular user sees limited stats
      stats.totalPosts = 0
      stats.totalUsers = 0
      stats.totalComments = 0
      stats.totalTags = 0
    }
  } catch (error: any) {
    console.error('Failed to load stats:', error)
    ElMessage.error('Failed to load statistics')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<template>
  <div class="home-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <h2>BroadBlog</h2>
          </div>
          <div class="nav">
            <el-menu
              mode="horizontal"
              :default-active="activeIndex"
              :ellipsis="false"
              @select="handleSelect"
            >
              <el-menu-item index="posts">Post Management</el-menu-item>
              <el-menu-item 
                v-if="userStore.isAdmin()" 
                index="users"
              >
                User Management
              </el-menu-item>
              <el-menu-item index="comments">Comment Management</el-menu-item>
              <el-menu-item index="tags">Tag Management</el-menu-item>
              <el-menu-item 
                v-if="userStore.isAdmin()" 
                index="admin"
              >
                Admin Panel
              </el-menu-item>
            </el-menu>
          </div>
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                {{ userStore.user?.username || 'User' }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">Profile</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>Logout</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <div class="welcome-section">
          <el-card>
            <template #header>
              <div class="welcome-header">
                <h3>Welcome to BroadBlog</h3>
                <p v-if="userStore.isAdmin()" class="role-badge admin">Administrator</p>
                <p v-else class="role-badge user">Regular User</p>
              </div>
            </template>
            <div class="welcome-content">
              <p>Where voices turn into communities</p>
              <p v-if="userStore.isAdmin()">
                Welcome to the admin panel
              </p>
              <p v-else>
                For creaters and storytellers
              </p>
            </div>
          </el-card>
        </div>
        
        <div class="stats-section">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ stats.totalPosts }}</div>
                    <div class="stat-label">Total Posts</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6" v-if="userStore.isAdmin()">
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-icon">
                    <el-icon><User /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ stats.totalUsers }}</div>
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
                    <div class="stat-number">{{ stats.totalComments }}</div>
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
                    <div class="stat-number">{{ stats.totalTags }}</div>
                    <div class="stat-label">Total Tags</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
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

.logo h2 {
  margin: 0;
  color: #409eff;
}

.nav {
  flex: 1;
  display: flex;
  justify-content: center;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.main-content {
  padding: 20px;
  background-color: #f5f5f5;
}

.welcome-section {
  margin-bottom: 20px;
}

.welcome-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome-header h3 {
  margin: 0;
  color: #409eff;
}

.role-badge {
  margin: 0;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.role-badge.admin {
  background-color: #f56c6c;
  color: white;
}

.role-badge.user {
  background-color: #67c23a;
  color: white;
}

.welcome-content {
  line-height: 1.6;
}

.stats-section {
  margin-top: 20px;
}

.stat-card {
  height: 120px;
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
</style>
