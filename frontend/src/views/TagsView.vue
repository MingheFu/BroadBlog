<template>
  <div class="tags-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>Tag Management</h2>
          <el-button 
            v-if="userStore.isAdmin()" 
            type="primary" 
            @click="showCreateDialog = true"
          >
            <el-icon><Plus /></el-icon>
            New Tag
          </el-button>
        </div>
      </el-header>
      
      <el-main>
        <!-- Tag Statistics -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon><Collection /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ tagStats.totalTags }}</div>
                  <div class="stat-label">Total Tags</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ tagStats.totalCategories }}</div>
                  <div class="stat-label">Categories</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon><TrendCharts /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ tagStats.mostUsedTag }}</div>
                  <div class="stat-label">Most Used</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ tagStats.recentTags }}</div>
                  <div class="stat-label">Recent</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- Tags Table -->
        <el-card>
          <el-table
            :data="tags"
            v-loading="loading"
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="Name" min-width="150" />
            <el-table-column prop="description" label="Description" min-width="200" />
            <el-table-column prop="categoryName" label="Category" width="120" />
            <el-table-column prop="usageCount" label="Usage Count" width="120" />
            <el-table-column prop="createdAt" label="Created At" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="200" fixed="right">
              <template #default="scope">
                <el-button
                  v-if="userStore.isAdmin()"
                  size="small"
                  @click="handleEdit(scope.row)"
                >
                  Edit
                </el-button>
                <el-button
                  v-if="userStore.isAdmin()"
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
      :title="isEditing ? 'Edit Tag' : 'New Tag'"
      width="600px"
    >
      <el-form
        ref="tagFormRef"
        :model="tagForm"
        :rules="tagRules"
        label-width="80px"
      >
        <el-form-item label="Name" prop="name">
          <el-input
            v-model="tagForm.name"
            placeholder="Enter tag name"
          />
        </el-form-item>
        
        <el-form-item label="Description" prop="description">
          <el-input
            v-model="tagForm.description"
            type="textarea"
            :rows="3"
            placeholder="Enter tag description"
          />
        </el-form-item>

        <el-form-item label="Category" prop="categoryId">
          <el-select
            v-model="tagForm.categoryId"
            placeholder="Select category"
            clearable
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
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

const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const showCreateDialog = ref(false)
const isEditing = ref(false)
const currentTagId = ref<number | null>(null)

const tags = ref([])
const categories = ref([])

const tagStats = reactive({
  totalTags: 0,
  totalCategories: 0,
  mostUsedTag: 0,
  recentTags: 0
})

const tagForm = reactive({
  name: '',
  description: '',
  categoryId: null
})

const tagRules = {
  name: [
    { required: true, message: 'Please enter tag name', trigger: 'blur' }
  ]
}

const tagFormRef = ref()

const loadTags = async () => {
  try {
    loading.value = true
    // TODO: Implement tag loading from backend
    tags.value = []
    tagStats.totalTags = 0
  } catch (error: any) {
    ElMessage.error('Failed to load tags')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    // TODO: Implement category loading from backend
    categories.value = []
  } catch (error: any) {
    console.error('Failed to load categories:', error)
  }
}

const handleEdit = (tag: any) => {
  isEditing.value = true
  currentTagId.value = tag.id
  tagForm.name = tag.name
  tagForm.description = tag.description
  tagForm.categoryId = tag.categoryId
  showCreateDialog.value = true
}

const handleDelete = async (tag: any) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete tag "${tag.name}"?`,
      'Confirm Delete',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    // TODO: Implement tag deletion
    ElMessage.success('Deleted successfully')
    await loadTags()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Delete failed')
    }
  }
}

const handleSubmit = async () => {
  if (!tagFormRef.value) return
  
  try {
    await tagFormRef.value.validate()
    submitLoading.value = true
    
    // TODO: Implement tag creation/update
    ElMessage.success(isEditing.value ? 'Updated successfully' : 'Created successfully')
    
    showCreateDialog.value = false
    resetForm()
    await loadTags()
  } catch (error: any) {
    ElMessage.error('Operation failed')
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  tagForm.name = ''
  tagForm.description = ''
  tagForm.categoryId = null
  isEditing.value = false
  currentTagId.value = null
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('en-US')
}

onMounted(() => {
  loadTags()
  loadCategories()
})
</script>

<style scoped>
.tags-container {
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
