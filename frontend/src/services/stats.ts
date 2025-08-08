import api from './api'

export interface SystemStats {
  totalPosts: number
  totalUsers: number
  totalComments: number
  totalTags: number
}

export const statsService = {
  // Get system statistics
  async getSystemStats(): Promise<SystemStats> {
    const response = await api.get('/admin/content/stats')
    return response.data
  }
}
