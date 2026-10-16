import axios from 'axios'
import { useUserStore } from '@/store/user'
import router from '@/router'

const instance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  (response) => {
    const data = response.data
    // 后端业务错误统一返回 HTTP 200 + { code: "500", msg }，这里转为 reject 便于页面统一 catch
    if (data && typeof data === 'object' && !(data instanceof Blob) && data.code && data.code !== '200') {
      const error: any = new Error(data.msg || '请求失败')
      error.response = response
      return Promise.reject(error)
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default instance
