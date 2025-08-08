import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authService, type AuthResponse } from '@/services/auth'

export interface User {
  id: number
  username: string
  email: string
  roles: string[]
}

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const isAuthenticated = ref(false)

  // Initialize from localStorage
  const initAuth = () => {
    const storedToken = authService.getToken()
    if (storedToken) {
      token.value = storedToken
      isAuthenticated.value = true
      // TODO: Fetch user info from backend
    }
  }

  const login = async (username: string, password: string) => {
    try {
      const response = await authService.login({ username, password })
      user.value = response.user
      token.value = response.token
      isAuthenticated.value = true
      return response
    } catch (error) {
      throw error
    }
  }

  const register = async (username: string, password: string, email: string) => {
    try {
      const response = await authService.register({ username, password, email })
      user.value = response.user
      token.value = response.token
      isAuthenticated.value = true
      return response
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    authService.logout()
    user.value = null
    token.value = null
    isAuthenticated.value = false
  }

  // Check if user has specific role
  const hasRole = (role: string): boolean => {
    return user.value?.roles?.includes(role) || false
  }

  // Check if user is admin
  const isAdmin = (): boolean => {
    return hasRole('ADMIN')
  }

  // Check if user is regular user
  const isUser = (): boolean => {
    return hasRole('USER')
  }

  return {
    user,
    token,
    isAuthenticated,
    initAuth,
    login,
    register,
    logout,
    hasRole,
    isAdmin,
    isUser
  }
}) 