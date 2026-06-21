<template>
  <view class="forgot-container">
    <view class="forgot-header">
      <text class="title">重置密码</text>
      <text class="subtitle">输入邮箱获取验证码</text>
    </view>
    
    <!-- 步骤1: 输入邮箱 -->
    <view v-if="step === 1" class="forgot-form">
      <view class="form-item">
        <input 
          v-model="email" 
          type="text" 
          placeholder="请输入注册邮箱"
          class="form-input"
        />
      </view>
      <button class="btn-primary" @click="handleSendCode" :loading="loading">
        获取验证码
      </button>
    </view>
    
    <!-- 步骤2: 输入验证码和新密码 -->
    <view v-else class="forgot-form">
      <view class="code-tip">
        <text>验证码已发送至 {{ email }}</text>
        <text class="code-hint">（开发环境：验证码显示在提示中）</text>
      </view>
      <view class="form-item">
        <input 
          v-model="code" 
          type="number" 
          placeholder="请输入6位验证码"
          class="form-input"
          maxlength="6"
        />
      </view>
      <view class="form-item">
        <input 
          v-model="newPassword" 
          type="password" 
          placeholder="请输入新密码（至少6位）"
          class="form-input"
        />
      </view>
      <view class="form-item">
        <input 
          v-model="confirmPassword" 
          type="password" 
          placeholder="请确认新密码"
          class="form-input"
        />
      </view>
      <button class="btn-primary" @click="handleResetPassword" :loading="loading">
        重置密码
      </button>
    </view>
    
    <view class="back-link" @click="goBack">
      <text>返回登录</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '../../api/modules'

const step = ref(1)
const loading = ref(false)
const email = ref('')
const code = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

const handleSendCode = async () => {
  if (!email.value) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' })
    return
  }
  
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    uni.showToast({ title: '请输入正确的邮箱格式', icon: 'none' })
    return
  }
  
  loading.value = true
  try {
    const res = await authApi.forgotPassword({ email: email.value })
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    // 开发环境显示验证码
    if (res.data) {
      setTimeout(() => {
        uni.showModal({
          title: '开发环境验证码',
          content: res.data,
          showCancel: false
        })
      }, 500)
    }
    step.value = 2
  } catch (error) {
    uni.showToast({ title: error.message || '发送失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleResetPassword = async () => {
  if (!code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  if (code.value.length !== 6) {
    uni.showToast({ title: '验证码为6位数字', icon: 'none' })
    return
  }
  if (!newPassword.value) {
    uni.showToast({ title: '请输入新密码', icon: 'none' })
    return
  }
  if (newPassword.value.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
    return
  }
  
  loading.value = true
  try {
    await authApi.resetPassword({
      email: email.value,
      code: code.value,
      newPassword: newPassword.value
    })
    uni.showToast({ title: '密码重置成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (error) {
    uni.showToast({ title: error.message || '重置失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.forgot-container {
  min-height: 100vh;
  background: #f8fafc;
  padding: 80rpx 40rpx;
}

.forgot-header {
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

.forgot-form {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  background: #f8fafc;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #1e293b;
}

.code-tip {
  text-align: center;
  margin-bottom: 24rpx;
}

.code-tip text {
  display: block;
  font-size: 26rpx;
  color: #64748b;
}

.code-hint {
  font-size: 22rpx;
  color: #94a3b8;
  margin-top: 8rpx;
}

.back-link {
  text-align: center;
  margin-top: 40rpx;
}

.back-link text {
  font-size: 28rpx;
  color: #6366f1;
}
</style>
