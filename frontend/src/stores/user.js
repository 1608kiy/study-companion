import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi, userApi } from '@/api/modules'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
  }

  const login = async (credentials) => {
    const res = await authApi.login(credentials)
    setToken(res.data.token)
    userInfo.value = res.data
    return res
  }

  const register = async (userData) => {
    const res = await authApi.register(userData)
    setToken(res.data.token)
    userInfo.value = res.data
    return res
  }

  const getProfile = async () => {
    const res = await userApi.getProfile()
    userInfo.value = res.data
    return res
  }

  const updateProfile = async (data) => {
    const res = await userApi.updateProfile(data)
    await getProfile()
    return res
  }

  const logout = async () => {
    try {
      await userApi.logout()
    } finally {
      clearToken()
      userInfo.value = null
    }
  }

  return {
    token,
    userInfo,
    setToken,
    clearToken,
    login,
    register,
    getProfile,
    updateProfile,
    logout,
  }
})
