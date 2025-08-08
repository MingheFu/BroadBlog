import api from './api'

export interface Post {
  id: number
  title: string
  content: string
  createdAt: string
  updatedAt: string
  author: {
    id: number
    username: string
  }
  tags?: string[]
}

export interface PostForm {
  title: string
  content: string
  tagIds?: number[]
}

export interface PostPageResponse {
  content: Post[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const postsService = {
  // Get posts with pagination
  async getPosts(page: number = 1, size: number = 10): Promise<PostPageResponse> {
    const response = await api.get(`/posts/page?page=${page}&size=${size}`)
    return response.data
  },

  // Get my posts
  async getMyPosts(page: number = 1, size: number = 10): Promise<PostPageResponse> {
    const response = await api.get(`/posts/my?page=${page}&size=${size}`)
    return response.data
  },

  // Get post by ID
  async getPost(id: number): Promise<Post> {
    const response = await api.get(`/posts/${id}`)
    return response.data
  },

  // Create new post
  async createPost(postData: PostForm): Promise<Post> {
    const response = await api.post('/posts', postData)
    return response.data
  },

  // Update post
  async updatePost(id: number, postData: PostForm): Promise<Post> {
    const response = await api.put(`/posts/${id}`, postData)
    return response.data
  },

  // Delete post
  async deletePost(id: number): Promise<void> {
    await api.delete(`/posts/${id}`)
  },

  // Search posts
  async searchPosts(keyword: string, page: number = 1, size: number = 10): Promise<PostPageResponse> {
    const response = await api.get(`/posts/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`)
    return response.data
  }
}
