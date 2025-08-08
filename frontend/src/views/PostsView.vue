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
          <el-row :gutter="20">
            <el-col :span="8">
              <el-input
                v-model="searchKeyword"
                placeholder="Search post title..."
                prefix-icon="Search"
                clearable
                @input="handleSearch"
              />
            </el-col>
            <el-col :span="4">
              <el-button type="primary" @click="handleSearch">Search</el-button>
            </el-col>
            <el-col :span="12" style="text-align: right;">
              <el-radio-group v-model="postFilter" @change="handleFilterChange">
                <el-radio-button label="all">All Posts</el-radio-button>
                <el-radio-button label="my">My Posts</el-radio-button>
              </el-radio-group>
            </el-col>
          </el-row>
        </el-card>

        <!-- Posts Table -->
        <el-card>
          <el-table
            :data="posts"
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
                  :disabled="!canEdit(scope.row)"
                >
                  Edit
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click="handleDelete(scope.row)"
                  :disabled="!canDelete(scope.row)"
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
              :page-sizes="[5, 10, 20, 50]"
              :total="totalElements"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { postsService, type Post, type PostForm, type PostPageResponse } from '@/services/posts'

const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const showCreateDialog = ref(false)
const isEditing = ref(false)
const searchKeyword = ref('')
const currentPostId = ref<number | null>(null)
const postFilter = ref('all')

// Pagination
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)

const posts = ref<Post[]>([])

const postForm = reactive<PostForm>({
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

const postFormRef = ref()

const loadPosts = async () => {
  try {
    loading.value = true
    let response: PostPageResponse
    
    if (searchKeyword.value) {
      response = await postsService.searchPosts(searchKeyword.value, currentPage.value, pageSize.value)
    } else if (postFilter.value === 'my') {
      response = await postsService.getMyPosts(currentPage.value, pageSize.value)
    } else {
      response = await postsService.getPosts(currentPage.value, pageSize.value)
    }
    
    posts.value = response.content
    totalElements.value = response.totalElements
    totalPages.value = response.totalPages
  } catch (error: any) {
    ElMessage.error('Failed to load posts')
    console.error('Load posts error:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadPosts()
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadPosts()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadPosts()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadPosts()
}

const canEdit = (post: Post): boolean => {
  return userStore.isAdmin() || post.author.id === userStore.user?.id
}

const canDelete = (post: Post): boolean => {
  return userStore.isAdmin() || post.author.id === userStore.user?.id
}

const handleEdit = (post: Post) => {
  if (!canEdit(post)) {
    ElMessage.warning('You can only edit your own posts')
    return
  }
  
  isEditing.value = true
  currentPostId.value = post.id
  postForm.title = post.title
  postForm.content = post.content
  showCreateDialog.value = true
}

const handleDelete = async (post: Post) => {
  if (!canDelete(post)) {
    ElMessage.warning('You can only delete your own posts')
    return
  }
  
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
    
    await postsService.deletePost(post.id)
    ElMessage.success('Deleted successfully')
    await loadPosts()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Delete failed')
      console.error('Delete error:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!postFormRef.value) return
  
  try {
    await postFormRef.value.validate()
    submitLoading.value = true
    
    if (isEditing.value && currentPostId.value) {
      await postsService.updatePost(currentPostId.value, postForm)
      ElMessage.success('Updated successfully')
    } else {
      await postsService.createPost(postForm)
      ElMessage.success('Created successfully')
    }
    
    showCreateDialog.value = false
    resetForm()
    await loadPosts()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'Operation failed')
    console.error('Submit error:', error)
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 