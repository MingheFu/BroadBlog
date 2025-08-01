<template>
  <div class="posts-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>文章管理</h2>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            新建文章
          </el-button>
        </div>
      </el-header>
      
      <el-main>
        <!-- Search Bar -->
        <el-card class="search-card">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索文章标题..."
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
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="author.username" label="作者" width="120" />
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="更新时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button
                  size="small"
                  @click="handleEdit(scope.row)"
                >
                  编辑
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click="handleDelete(scope.row)"
                >
                  删除
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
      :title="isEditing ? '编辑文章' : '新建文章'"
      width="800px"
    >
      <el-form
        ref="postFormRef"
        :model="postForm"
        :rules="postRules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="postForm.title"
            placeholder="请输入文章标题"
          />
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入文章内容"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            {{ isEditing ? '更新' : '创建' }}
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
    { required: true, message: '请输入文章标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' }
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
    ElMessage.error('加载文章失败')
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
      `确定要删除文章 "${post.title}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.delete(`/posts/${post.id}`)
    ElMessage.success('删除成功')
    await loadPosts()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
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
      ElMessage.success('更新成功')
    } else {
      await api.post('/posts', postForm)
      ElMessage.success('创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    await loadPosts()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
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
  return new Date(dateString).toLocaleString('zh-CN')
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