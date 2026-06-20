<template>
  <view class="register-container">
    <view class="register-header">
      <text class="title">注册账号</text>
      <text class="subtitle">开始你的学习之旅</text>
    </view>
    
    <view class="register-form">
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
          placeholder="请输入密码（至少6位）"
          class="input"
        />
      </view>
      <view class="form-item">
        <input 
          v-model="form.confirmPassword" 
          type="password" 
          placeholder="请确认密码"
          class="input"
        />
      </view>
      <view class="form-item">
        <input 
          v-model="form.nickname" 
          type="text" 
          placeholder="请输入昵称（可选）"
          class="input"
        />
      </view>
      <button class="btn-register" @click="handleRegister" :loading="loading">
        注册
      </button>
      <view class="login-link" @click="goLogin">
        已有账号？立即登录
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
  password: '',
  confirmPassword: '',
  nickname: ''
})

const handleRegister = async () => {
  if (!form.value.email || !form.value.password) {
    uni.showToast({ title: '请输入邮箱和密码', icon: 'none' })
    return
  }
  
  if (form.value.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }
  
  if (form.value.password !== form.value.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  
  loading.value = true
  try {
    await userStore.register({
      email: form.value.email,
      password: form.value.password,
      nickname: form.value.nickname || undefined
    })
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/home' })
    }, 1000)
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

const goLogin = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: #f8fafc;
  padding: 80rpx 40rpx;
}

.register-header {
  text-align: center;
  margin-bottom: 60rpx;
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

.register-form {
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

.btn-register {
  width: 100%;
  height: 88rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 12rpx;
  font-size: 30rpx;
  font-weight: 600;
  margin-top: 20rpx;
}

.btn-register:active {
  background: #4f46e5;
}

.login-link {
  text-align: center;
  margin-top: 30rpx;
  font-size: 26rpx;
  color: #6366f1;
}
</style>
