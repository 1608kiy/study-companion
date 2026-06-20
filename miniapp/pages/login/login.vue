<template>
  <view class="login-container">
    <view class="login-header">
      <view class="logo-placeholder">
        <text class="logo-icon">📚</text>
      </view>
      <text class="title">智学伴</text>
      <text class="subtitle">记录努力，看见进步</text>
    </view>
    
    <view class="login-form">
      <view class="form-item">
        <input 
          v-model="form.email" 
          type="text" 
          placeholder="请输入邮箱"
          class="input"
        />
      </view>
      <view class="form-item">
        <input 
          v-model="form.password" 
          type="password" 
          placeholder="请输入密码"
          class="input"
        />
      </view>
      <button class="btn-login" @click="handleLogin" :loading="loading">
        登录
      </button>
      <view class="register-link" @click="goRegister">
        还没有账号？立即注册
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const loading = ref(false)

const form = ref({
  email: '',
  password: ''
})

const validateEmail = (email) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

const handleLogin = async () => {
  if (!form.value.email || !form.value.password) {
    uni.showToast({ title: '请输入邮箱和密码', icon: 'none' })
    return
  }
  
  if (!validateEmail(form.value.email)) {
    uni.showToast({ title: '请输入正确的邮箱格式', icon: 'none' })
    return
  }
  
  if (form.value.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }
  
  loading.value = true
  try {
    await userStore.login(form.value)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/home' })
    }, 1000)
  } catch (error) {
    uni.showToast({ title: error.message || '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  uni.navigateTo({ url: '/pages/register/register' })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: #f8fafc;
  padding: 80rpx 40rpx;
}

.login-header {
  text-align: center;
  margin-bottom: 80rpx;
}

.logo-placeholder {
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto 20rpx;
  background: #eef2ff;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon {
  font-size: 80rpx;
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12rpx;
}

.subtitle {
  display: block;
  font-size: 28rpx;
  color: #64748b;
}

.login-form {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.input {
  width: 100%;
  height: 88rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  border: 1rpx solid #e2e8f0;
}

.btn-login {
  width: 100%;
  height: 88rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 12rpx;
  font-size: 30rpx;
  font-weight: 600;
  margin-top: 20rpx;
}

.btn-login:active {
  background: #4f46e5;
}

.register-link {
  text-align: center;
  margin-top: 30rpx;
  font-size: 26rpx;
  color: #6366f1;
}
</style>
