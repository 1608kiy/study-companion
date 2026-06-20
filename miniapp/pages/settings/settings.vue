<template>
  <view class="settings-container">
    <!-- 个人信息 -->
    <view class="card">
      <text class="card-title">个人信息</text>
      <view class="form-item">
        <text class="form-label">昵称</text>
        <input 
          v-model="form.nickname" 
          class="form-input" 
          placeholder="请输入昵称"
        />
      </view>
    </view>
    
    <!-- 学习目标 -->
    <view class="card">
      <text class="card-title">学习目标</text>
      <view class="form-item">
        <text class="form-label">每日目标(分钟)</text>
        <input 
          v-model="form.dailyGoal" 
          type="number" 
          class="form-input" 
          placeholder="120"
        />
      </view>
      <view class="form-item">
        <text class="form-label">每周目标(分钟)</text>
        <input 
          v-model="form.weeklyGoal" 
          type="number" 
          class="form-input" 
          placeholder="840"
        />
      </view>
      <view class="form-item">
        <text class="form-label">每月目标(分钟)</text>
        <input 
          v-model="form.monthlyGoal" 
          type="number" 
          class="form-input" 
          placeholder="3600"
        />
      </view>
    </view>
    
    <!-- 保存按钮 -->
    <button class="btn-save" @click="handleSave" :loading="saving">
      保存设置
    </button>
    
    <!-- 账号操作 -->
    <view class="card">
      <text class="card-title">账号操作</text>
      <button class="btn-logout" @click="handleLogout">
        退出登录
      </button>
      <button class="btn-delete" @click="handleDeleteAccount">
        注销账号
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import { userApi } from '../../api/modules'

const userStore = useUserStore()
const saving = ref(false)

const form = ref({
  nickname: '',
  dailyGoal: 120,
  weeklyGoal: 840,
  monthlyGoal: 3600
})

const validateForm = () => {
  if (!form.value.nickname?.trim()) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return false
  }
  
  const daily = Number(form.value.dailyGoal)
  const weekly = Number(form.value.weeklyGoal)
  const monthly = Number(form.value.monthlyGoal)
  
  if (isNaN(daily) || daily < 1 || daily > 1440) {
    uni.showToast({ title: '每日目标应在1-1440分钟之间', icon: 'none' })
    return false
  }
  
  if (isNaN(weekly) || weekly < 1 || weekly > 10080) {
    uni.showToast({ title: '每周目标应在1-10080分钟之间', icon: 'none' })
    return false
  }
  
  if (isNaN(monthly) || monthly < 1 || monthly > 43200) {
    uni.showToast({ title: '每月目标应在1-43200分钟之间', icon: 'none' })
    return false
  }
  
  return true
}

const handleSave = async () => {
  if (!validateForm()) return
  
  saving.value = true
  try {
    await userStore.updateProfile({
      nickname: form.value.nickname.trim(),
      dailyGoal: Number(form.value.dailyGoal),
      weeklyGoal: Number(form.value.weeklyGoal),
      monthlyGoal: Number(form.value.monthlyGoal)
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

const handleLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await userStore.logout()
          uni.reLaunch({ url: '/pages/login/login' })
        } catch (error) {
          uni.showToast({ title: error.message || '退出失败', icon: 'none' })
        }
      }
    }
  })
}

const handleDeleteAccount = () => {
  uni.showModal({
    title: '危险操作',
    content: '注销账号将删除所有数据，此操作不可恢复！',
    confirmText: '确认注销',
    confirmColor: '#ef4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          await userApi.deleteAccount()
          await userStore.logout()
          uni.reLaunch({ url: '/pages/login/login' })
          uni.showToast({ title: '账号已注销', icon: 'success' })
        } catch (error) {
          uni.showToast({ title: error.message || '注销失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  if (userStore.userInfo) {
    form.value = {
      nickname: userStore.userInfo.nickname || '',
      dailyGoal: userStore.userInfo.dailyGoal || 120,
      weeklyGoal: userStore.userInfo.weeklyGoal || 840,
      monthlyGoal: userStore.userInfo.monthlyGoal || 3600
    }
  }
})
</script>

<style scoped>
.settings-container {
  padding: 20rpx;
  background: #f8fafc;
  min-height: 100vh;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid #e2e8f0;
}

.card-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 12rpx;
}

.form-input {
  width: 100%;
  height: 80rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  border: 1rpx solid #e2e8f0;
}

.btn-save {
  width: 100%;
  height: 88rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
}

.btn-logout {
  width: 100%;
  height: 88rpx;
  background: #fff;
  color: #6366f1;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: 1rpx solid #6366f1;
  margin-bottom: 16rpx;
}

.btn-delete {
  width: 100%;
  height: 88rpx;
  background: #fff;
  color: #ef4444;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: 1rpx solid #ef4444;
}
</style>
