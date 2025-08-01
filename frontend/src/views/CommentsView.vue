<template>
  <div class="comments-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>Comment Management</h2>
        </div>
      </el-header>
      
      <el-main>
        <el-card>
          <el-table
            :data="comments"
            v-loading="loading"
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="content" label="Comment Content" min-width="300" />
            <el-table-column prop="author.username" label="Author" width="120" />
            <el-table-column prop="post.title" label="Post Title" width="200" />
            <el-table-column prop="createdAt" label="Created At" width="180">
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

interface Comment {
  id: number
  content: string
  createdAt: string
  author: {
    id: number
    username: string
  }
  post: {
    id: number
    title: string
  }
}

const loading = ref(false)
const comments = ref<Comment[]>([])

const loadComments = async () => {
  try {
    loading.value = true
    const response = await api.get('/comments')
    comments.value = response.data
  } catch (error: any) {
    ElMessage.error('Failed to load comments')
  } finally {
    loading.value = false
  }
}

const handleView = (comment: Comment) => {
  ElMessage.info(`View comment: ${comment.content.substring(0, 50)}...`)
}

const handleDelete = async (comment: Comment) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete this comment?`,
      'Confirm Delete',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    await api.delete(`/comments/${comment.id}`)
    ElMessage.success('Deleted successfully')
    await loadComments()
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
  loadComments()
})
</script>

<style scoped>
.comments-container {
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