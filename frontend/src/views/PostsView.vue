<template>
  <div class="posts-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>Post Management</h2>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            New Post
          </el-button>
        </div>
      </el-header>
      
      <el-main>
        <!-- Search Bar -->
        <el-card class="search-card">
          <el-input
            v-model="searchKeyword"
            placeholder="Search post title..."
            prefix-icon="Search"
            clearable
            @input="handleSearch"
            style="width: 300px"
          />
        </el-card>

        <!-- Posts Table -->
        <el-card>
          <el-table
            :data="filteredPosts"
            v-loading="loading"
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column prop="author.username" label="Author" width="120" />
            <el-table-column prop="createdAt" label="Created At" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="Updated At" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="200" fixed="right">
              <template #default="scope">
                <el-button
                  size="small"
                  @click="handleEdit(scope.row)"
                >
                  Edit
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

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEditing ? 'Edit Post' : 'New Post'"
      width="800px"
    >
      <el-form
        ref="postFormRef"
        :model="postForm"
        :rules="postRules"
        label-width="80px"
      >
        <el-form-item label="Title" prop="title">
          <el-input
            v-model="postForm.title"
            placeholder="Enter post title"
          />
        </el-form-item>
        
        <el-form-item label="Content" prop="content">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="10"
            placeholder="Enter post content"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">Cancel</el-button>
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            {{ isEditing ? 'Update' : 'Create' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/services/api'

interface Post {
  id: number
  title: string
  content: string
  createdAt: string
  updatedAt: string
  author: {
    id: number
    username: string
  }
}

const loading = ref(false)
const submitLoading = ref(false)
const showCreateDialog = ref(false)
const isEditing = ref(false)
const searchKeyword = ref('')
const currentPostId = ref<number | null>(null)

const posts = ref<Post[]>([])

const postForm = reactive({
  title: '',
  content: ''
})

const postRules = {
  title: [
    { required: true, message: 'Please enter post title', trigger: 'blur' }
  ],
  content: [
    { required: true, message: 'Please enter post content', trigger: 'blur' }
  ]
}

const filteredPosts = computed(() => {
  if (!searchKeyword.value) {
    return posts.value
  }
  return posts.value.filter(post =>
    post.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const postFormRef = ref()

const loadPosts = async () => {
  try {
    loading.value = true
    const response = await api.get('/posts')
    posts.value = response.data
  } catch (error: any) {
    ElMessage.error('Failed to load posts')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // Search is handled by computed property
}

const handleEdit = (post: Post) => {
  isEditing.value = true
  currentPostId.value = post.id
  postForm.title = post.title
  postForm.content = post.content
  showCreateDialog.value = true
}

const handleDelete = async (post: Post) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete post "${post.title}"?`,
      'Confirm Delete',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    await api.delete(`/posts/${post.id}`)
    ElMessage.success('Deleted successfully')
    await loadPosts()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Delete failed')
    }
  }
}

const handleSubmit = async () => {
  if (!postFormRef.value) return
  
  try {
    await postFormRef.value.validate()
    submitLoading.value = true
    
    if (isEditing.value && currentPostId.value) {
      await api.put(`/posts/${currentPostId.value}`, postForm)
      ElMessage.success('Updated successfully')
    } else {
      await api.post('/posts', postForm)
      ElMessage.success('Created successfully')
    }
    
    showCreateDialog.value = false
    resetForm()
    await loadPosts()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'Operation failed')
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  postForm.title = ''
  postForm.content = ''
  isEditing.value = false
  currentPostId.value = null
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('en-US')
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.posts-container {
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

.search-card {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 