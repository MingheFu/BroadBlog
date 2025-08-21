import api from './api'

export interface Comment {
  id: number
  content: string
  createdAt: string
  updatedAt: string
  author: {
    id: number
    username: string
    avatar?: string
  }
  post: {
    id: number
    title: string
  }
  parentComment?: {
    id: number
    content: string
  }
  replies?: Comment[]
}

export interface CommentForm {
  content: string
  postId: number
  parentCommentId?: number
}

export const commentService = {
  // 获取所有评论
  async getAllComments(): Promise<Comment[]> {
    const response = await api.get('/comments')
    return response.data
  },

  // 根据ID获取评论
  async getCommentById(id: number): Promise<Comment> {
    const response = await api.get(`/comments/${id}`)
    return response.data
  },

  // 根据作者ID获取评论
  async getCommentsByAuthorId(authorId: number): Promise<Comment[]> {
    const response = await api.get(`/comments/author/${authorId}`)
    return response.data
  },

  // 根据帖子ID获取评论
  async getCommentsByPostId(postId: number): Promise<Comment[]> {
    const response = await api.get(`/comments/post/${postId}`)
    return response.data
  },

  // 创建评论
  async createComment(commentData: CommentForm): Promise<Comment> {
    const response = await api.post('/comments', commentData)
    return response.data
  },

  // 更新评论
  async updateComment(id: number, commentData: Partial<CommentForm>): Promise<Comment> {
    const response = await api.put(`/comments/${id}`, commentData)
    return response.data
  },

  // 删除评论
  async deleteComment(id: number): Promise<void> {
    await api.delete(`/comments/${id}`)
  },

  // 回复评论
  async replyToComment(parentCommentId: number, content: string, postId: number): Promise<Comment> {
    const commentData: CommentForm = {
      content,
      postId,
      parentCommentId
    }
    return this.createComment(commentData)
  }
}
