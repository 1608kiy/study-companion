<template>
  <view class="page-container">
    <!-- 骨架屏 -->
    <view v-if="loading" class="skeleton-wrapper">
      <view class="skeleton-card"></view>
      <view class="skeleton-card"></view>
      <view class="skeleton-btn"></view>
      <view class="skeleton-card"></view>
    </view>
    
    <!-- 实际内容 -->
    <view v-else>
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
      <button class="btn-primary" @click="handleSave" :loading="saving">
        保存设置
      </button>
      
      <!-- 账号操作 -->
      <view class="card">
        <text class="card-title">账号操作</text>
        <button class="btn-secondary" style="margin-bottom: 16rpx;" @click="handleLogout">
          退出登录
        </button>
        <button class="btn-danger" @click="handleDeleteAccount">
          注销账号
        </button>
      </view>
      
      <!-- 版本信息 -->
      <view class="card version-card" @click="showVersionInfo">
        <text class="version-text">版本：v{{ currentVersion }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import { userApi } from '../../api/modules'
import { showVersionInfo, getCurrentVersion } from '../../utils/version'

const userStore = useUserStore()
const loading = ref(true)
const saving = ref(false)
const currentVersion = getCurrentVersion()

const form = ref({
  nickname: '',
  dailyGoal: 120,
  weeklyGoal: 840,
  monthlyGoal: 3600
})

// 监听 userInfo 变化，更新表单
watch(() => userStore.userInfo, (newInfo) => {
  if (newInfo) {
    form.value = {
      nickname: newInfo.nickname || '',
      dailyGoal: newInfo.dailyGoal || 120,
      weeklyGoal: newInfo.weeklyGoal || 840,
      monthlyGoal: newInfo.monthlyGoal || 3600
    }
  }
}, { immediate: true })

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
          // 先显示提示，再跳转
          uni.showToast({ title: '账号已注销', icon: 'success', duration: 1500 })
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/login' })
          }, 1500)
        } catch (error) {
          uni.showToast({ title: error.message || '注销失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  // 如果 userInfo 已经加载完成
  if (userStore.userInfo) {
    form.value = {
      nickname: userStore.userInfo.nickname || '',
      dailyGoal: userStore.userInfo.dailyGoal || 120,
      weeklyGoal: userStore.userInfo.weeklyGoal || 840,
      monthlyGoal: userStore.userInfo.monthlyGoal || 3600
    }
  }
  loading.value = false
})
</script>

<style scoped>
.version-card {
  text-align: center;
  padding: 24rpx;
}

.version-text {
  font-size: 24rpx;
  color: #6b7280;
}

.skeleton-card {
  height: 200rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.skeleton-btn {
  height: 88rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}
</style>
