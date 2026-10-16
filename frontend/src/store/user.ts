import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/utils/axios'

export interface User {
  id: number
  username: string
  nickname: string
  name?: string | null
  avatar: string
  phone: string
  email: string
  age?: number | null
  role: string
  status: string
  createTime: string
  updateTime: string
}

export interface LoginResponse {
  token: string
  role: string
  user: User
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const user = ref<User | null>(null)

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  const login = async (username: string, password: string) => {
    const response = await axios.post('/auth/login', { username, password })
    const data = response.data.data as LoginResponse
    token.value = data.token
    user.value = data.user
    localStorage.setItem('resource_share_token', data.token)
    localStorage.setItem('resource_share_user', JSON.stringify(data.user))
    axios.defaults.headers.common['Authorization'] = `Bearer ${data.token}`
  }

  const register = async (formData: FormData) => {
    await axios.post('/auth/register', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }

  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('resource_share_token')
    localStorage.removeItem('resource_share_user')
    delete axios.defaults.headers.common['Authorization']
  }

  const loadFromStorage = () => {
    const storedToken = localStorage.getItem('resource_share_token')
    const storedUser = localStorage.getItem('resource_share_user')
    if (storedToken && storedUser) {
      token.value = storedToken
      user.value = JSON.parse(storedUser)
      axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`
    }
  }

  const setUser = (u: User) => {
    user.value = u
    localStorage.setItem('resource_share_user', JSON.stringify(u))
  }

  const updateProfile = async (profileData: Partial<User>) => {
    const response = await axios.put('/users/me', profileData)
    user.value = response.data.data
    localStorage.setItem('resource_share_user', JSON.stringify(user.value))
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    loadFromStorage,
    setUser,
    updateProfile
  }
})
