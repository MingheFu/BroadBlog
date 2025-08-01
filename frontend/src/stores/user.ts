import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authService, type AuthResponse } from '@/services/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref<AuthResponse['user'] | null>(null)
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

  return {
    user,
    token,
    isAuthenticated,
    initAuth,
    login,
    register,
    logout
  }
}) 