// 用户状态管理
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi } from '../api/modules'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname || '学习者')

  // 设置 Token
  const setToken = (newToken) => {
    token.value = newToken
    uni.setStorageSync('token', newToken)
  }

  // 清除 Token
  const clearToken = () => {
    token.value = ''
    userInfo.value = null
    uni.removeStorageSync('token')
  }

  // 登录
  const login = async (credentials) => {
    const res = await authApi.login(credentials)
    setToken(res.data.token)
    userInfo.value = res.data
    return res
  }

  // 注册
  const register = async (userData) => {
    const res = await authApi.register(userData)
    setToken(res.data.token)
    userInfo.value = res.data
    return res
  }

  // 获取用户信息
  const getProfile = async () => {
    const res = await userApi.getProfile()
    userInfo.value = res.data
    return res
  }

  // 更新用户信息
  const updateProfile = async (data) => {
    const res = await userApi.updateProfile(data)
    await getProfile()
    return res
  }

  // 退出登录
  const logout = async () => {
    try {
      await userApi.logout()
    } finally {
      clearToken()
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    nickname,
    setToken,
    clearToken,
    login,
    register,
    getProfile,
    updateProfile,
    logout
  }
})
