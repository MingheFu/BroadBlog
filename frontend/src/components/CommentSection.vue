<template>
  <div class="comment-section">
    <h3>Comments ({{ comments.length }})</h3>
    
    <!-- Comment Form -->
    <div class="comment-form">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="Write a comment..."
        :disabled="!userStore.isAuthenticated"
      />
      <div class="comment-form-actions">
        <el-button
          type="primary"
          @click="submitComment"
          :disabled="!newComment.trim() || !userStore.isAuthenticated"
          :loading="submitting"
        >
          Post Comment
        </el-button>
      </div>
    </div>

    <!-- Comments List -->
    <div class="comments-list">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <div class="comment-header">
          <div class="comment-author">
            <el-avatar :size="32" :src="comment.author.avatar">
              {{ comment.author.username.charAt(0).toUpperCase() }}
            </el-avatar>
            <span class="author-name">{{ comment.author.username }}</span>
            <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
          </div>
          <div class="comment-actions">
            <el-button
              type="text"
              size="small"
              @click="showReplyForm(comment.id)"
            >
              Reply
            </el-button>
            <el-button
              v-if="canDelete(comment)"
              type="text"
              size="small"
              @click="deleteComment(comment.id)"
            >
              Delete
            </el-button>
          </div>
        </div>
        
        <div class="comment-content">
          {{ comment.content }}
        </div>

        <!-- Reply Form -->
        <div v-if="replyingTo === comment.id" class="reply-form">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="2"
            placeholder="Write a reply..."
          />
          <div class="reply-form-actions">
            <el-button
              type="primary"
              size="small"
              @click="submitReply(comment.id)"
              :disabled="!replyContent.trim()"
              :loading="submitting"
            >
              Reply
            </el-button>
            <el-button
              size="small"
              @click="cancelReply"
            >
              Cancel
            </el-button>
          </div>
        </div>

        <!-- Replies -->
        <div v-if="comment.replies && comment.replies.length > 0" class="replies">
          <div
            v-for="reply in comment.replies"
            :key="reply.id"
            class="reply-item"
          >
            <div class="reply-header">
              <div class="reply-author">
                <el-avatar :size="24" :src="reply.author.avatar">
                  {{ reply.author.username.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="author-name">{{ reply.author.username }}</span>
                <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
              </div>
              <div class="reply-actions">
                <el-button
                  v-if="canDelete(reply)"
                  type="text"
                  size="small"
                  @click="deleteComment(reply.id)"
                >
                  Delete
                </el-button>
              </div>
            </div>
            <div class="reply-content">
              {{ reply.content }}
            </div>
          </div>
        </div>
      </div>

      <div v-if="comments.length === 0" class="no-comments">
        No comments yet. Be the first to comment!
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { commentService, type Comment } from '@/services/comments'
import { websocketService } from '@/services/websocket'

interface Props {
  postId: number
}

const props = defineProps<Props>()
const userStore = useUserStore()

const comments = ref<Comment[]>([])
const newComment = ref('')
const replyContent = ref('')
const replyingTo = ref<number | null>(null)
const submitting = ref(false)

const loadComments = async () => {
  try {
    const postComments = await commentService.getCommentsByPostId(props.postId)
    comments.value = postComments
  } catch (error) {
    console.error('Failed to load comments:', error)
    ElMessage.error('Failed to load comments')
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  
  try {
    submitting.value = true
    await commentService.createComment({
      content: newComment.value,
      postId: props.postId
    })
    
    newComment.value = ''
    await loadComments()
    ElMessage.success('Comment posted successfully')
  } catch (error) {
    console.error('Failed to post comment:', error)
    ElMessage.error('Failed to post comment')
  } finally {
    submitting.value = false
  }
}

const showReplyForm = (commentId: number) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('Please login to reply')
    return
  }
  replyingTo.value = commentId
  replyContent.value = ''
}

const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

const submitReply = async (parentCommentId: number) => {
  if (!replyContent.value.trim()) return
  
  try {
    submitting.value = true
    await commentService.replyToComment(parentCommentId, replyContent.value, props.postId)
    
    replyContent.value = ''
    replyingTo.value = null
    await loadComments()
    ElMessage.success('Reply posted successfully')
  } catch (error) {
    console.error('Failed to post reply:', error)
    ElMessage.error('Failed to post reply')
  } finally {
    submitting.value = false
  }
}

const deleteComment = async (commentId: number) => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to delete this comment?',
      'Confirm Delete',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    await commentService.deleteComment(commentId)
    await loadComments()
    ElMessage.success('Comment deleted successfully')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete comment:', error)
      ElMessage.error('Failed to delete comment')
    }
  }
}

const canDelete = (comment: Comment): boolean => {
  if (!userStore.user) return false
  return userStore.isAdmin() || comment.author.id === userStore.user.id
}

const formatTime = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return 'Just now'
  if (minutes < 60) return `${minutes}m ago`
  if (hours < 24) return `${hours}h ago`
  if (days < 7) return `${days}d ago`
  
  return date.toLocaleDateString()
}

// WebSocket event handlers
const handleNewComment = (comment: Comment) => {
  if (comment.post.id === props.postId) {
    comments.value.unshift(comment)
    ElMessage.info(`New comment from ${comment.author.username}`)
  }
}

onMounted(() => {
  loadComments()
  
  // Setup WebSocket handlers
  websocketService.onComment = handleNewComment
})

onUnmounted(() => {
  websocketService.onComment = null
})
</script>

<style scoped>
.comment-section {
  margin-top: 24px;
}

.comment-section h3 {
  margin-bottom: 16px;
  color: #333;
}

.comment-form {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.comment-form-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.comments-list {
  space-y: 16px;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-content {
  margin-bottom: 12px;
  line-height: 1.5;
  color: #333;
}

.reply-form {
  margin: 12px 0;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.reply-form-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.replies {
  margin-left: 32px;
  margin-top: 12px;
}

.reply-item {
  padding: 8px 0;
  border-left: 2px solid #e4e7ed;
  padding-left: 12px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.reply-author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.reply-time {
  font-size: 11px;
  color: #999;
}

.reply-content {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-style: italic;
}
</style>
