<template>
  <div class="users-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>User Management</h2>
        </div>
      </el-header>
      
      <el-main>
        <el-card>
          <el-table
            :data="users"
            v-loading="loading"
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="Username" width="150" />
            <el-table-column prop="email" label="Email" min-width="200" />
            <el-table-column prop="createdAt" label="Registration Time" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="150" fixed="right">
              <template #default="scope">
                <el-button
                  size="small"
                  @click="handleView(scope.row)"
                >
                  View
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click="handleDelete(scope.row)"
                >
                  Delete
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/services/api'

interface User {
  id: number
  username: string
  email: string
  createdAt: string
}

const loading = ref(false)
const users = ref<User[]>([])

const loadUsers = async () => {
  try {
    loading.value = true
    const response = await api.get('/users')
    users.value = response.data
  } catch (error: any) {
    ElMessage.error('Failed to load users')
  } finally {
    loading.value = false
  }
}

const handleView = (user: User) => {
  ElMessage.info(`View user: ${user.username}`)
}

const handleDelete = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete user "${user.username}"?`,
      'Confirm Delete',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    await api.delete(`/users/${user.id}`)
    ElMessage.success('Deleted successfully')
    await loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Delete failed')
    }
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('en-US')
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.users-container {
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
</style> 