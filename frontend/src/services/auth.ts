import api from './api'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
}

export interface User {
  id: number
  username: string
  email: string
  roles: string[]
}

export interface AuthResponse {
  token: string
  refreshToken?: string
  user: User
  message?: string
}

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/login', credentials)
    const data = response.data
    localStorage.setItem('token', data.token)
    if (data.refreshToken) {
      localStorage.setItem('refreshToken', data.refreshToken)
    }
    return data
  },

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/register', userData)
    const data = response.data
    localStorage.setItem('token', data.token)
    if (data.refreshToken) {
      localStorage.setItem('refreshToken', data.refreshToken)
    }
    return data
  },

  logout(): void {
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  },

  getToken(): string | null {
    return localStorage.getItem('token')
  },

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken')
  },

  isAuthenticated(): boolean {
    return !!this.getToken()
  }
} 