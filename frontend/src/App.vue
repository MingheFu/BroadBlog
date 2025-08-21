<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { websocketService } from '@/services/websocket'
import NotificationBell from '@/components/NotificationBell.vue'

const userStore = useUserStore()

onMounted(async () => {
  await userStore.initAuth()
  
  // Connect WebSocket if user is authenticated
  if (userStore.isAuthenticated) {
    websocketService.connect()
  }
})
</script>

<template>
  <div id="app">
    <el-container>
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo-section">
            <img alt="Vue logo" class="logo" src="@/assets/logo.svg" width="40" height="40" />
            <h1 class="app-title">BroadBlog</h1>
          </div>

          <nav class="main-nav">
            <RouterLink to="/" class="nav-link">Home</RouterLink>
            <RouterLink to="/posts" class="nav-link">Posts</RouterLink>
            <RouterLink to="/comments" class="nav-link">Comments</RouterLink>
            <RouterLink to="/notifications" class="nav-link">Notifications</RouterLink>
            <RouterLink v-if="userStore.isAdmin()" to="/users" class="nav-link">Users</RouterLink>
            <RouterLink v-if="userStore.isAdmin()" to="/admin" class="nav-link">Admin</RouterLink>
          </nav>

          <div class="user-section">
            <NotificationBell v-if="userStore.isAuthenticated" />
            
            <div v-if="userStore.isAuthenticated" class="user-info">
              <span class="username">{{ userStore.user?.username }}</span>
              <el-button type="text" @click="userStore.logout()">Logout</el-button>
            </div>
            
            <RouterLink v-else to="/login" class="nav-link">Login</RouterLink>
          </div>
        </div>
      </el-header>

      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
#app {
  min-height: 100vh;
}

.app-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  display: block;
}

.app-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-link {
  text-decoration: none;
  color: #606266;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav-link:hover {
  color: #409eff;
  background-color: #f0f8ff;
}

.nav-link.router-link-active {
  color: #409eff;
  background-color: #f0f8ff;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-weight: 500;
  color: #333;
}

.app-main {
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
  padding: 20px;
}
</style>
