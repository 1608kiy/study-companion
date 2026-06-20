<template>
  <div class="settings-container">
    <el-row :gutter="20">
      <el-col :xs="24" :md="16">
        <el-card>
          <template #header>
            <span class="card-title">个人设置</span>
          </template>
          
          <el-form :model="profileForm" label-width="120px" class="settings-form">
            <div class="avatar-section">
              <el-avatar :size="80" :src="profileForm.avatar || undefined" icon="User" class="profile-avatar" />
              <div class="avatar-info">
                <el-upload
                  action="/api/v1/upload/avatar"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :on-error="handleAvatarError"
                  :headers="uploadHeaders"
                >
                  <el-button type="primary" size="small">更换头像</el-button>
                </el-upload>
                <span class="avatar-tip">支持 JPG、PNG 格式</span>
              </div>
            </div>
            
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" size="large" />
            </el-form-item>
            
            <el-divider content-position="left">学习目标</el-divider>
            
            <el-form-item label="每日目标(分钟)">
              <el-input-number 
                v-model="profileForm.dailyGoal" 
                :min="30" 
                :max="480" 
                :step="30"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="每周目标(分钟)">
              <el-input-number 
                v-model="profileForm.weeklyGoal" 
                :min="210" 
                :max="3360" 
                :step="60"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="每月目标(分钟)">
              <el-input-number 
                v-model="profileForm.monthlyGoal" 
                :min="900" 
                :max="14400" 
                :step="300"
                size="large"
              />
            </el-form-item>
            
            <el-divider content-position="left">其他设置</el-divider>
            
            <el-form-item label="深色模式">
              <el-switch v-model="profileForm.darkMode" @change="toggleDarkMode" />
            </el-form-item>
            
            <el-form-item label="消息通知">
              <el-switch v-model="profileForm.notificationEnabled" disabled />
              <span class="setting-hint">即将上线</span>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving" size="large" class="save-btn">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="8">
        <el-card>
          <template #header>
            <span class="card-title">账号操作</span>
          </template>
          
          <div class="account-actions">
            <el-button type="danger" plain @click="handleLogout" class="action-btn">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
            
            <el-button type="danger" @click="handleDeleteAccount" class="action-btn">
              <el-icon><Delete /></el-icon>
              注销账号
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const saving = ref(false)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`,
}))

const profileForm = ref({
  nickname: '',
  avatar: '',
  dailyGoal: 120,
  weeklyGoal: 840,
  monthlyGoal: 3600,
  darkMode: false,
  notificationEnabled: true,
})

const handleAvatarSuccess = (response) => {
  profileForm.value.avatar = response.data.url
  ElMessage.success('头像更新成功')
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败')
}

const toggleDarkMode = (value) => {
  document.documentElement.setAttribute('data-theme', value ? 'dark' : '')
  localStorage.setItem('darkMode', value ? 'true' : 'false')
}

const saveProfile = async () => {
  saving.value = true
  try {
    await userStore.updateProfile(profileForm.value)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    await userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出失败:', error)
    }
  }
}

const handleDeleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '注销账号将删除所有数据，此操作不可恢复！',
      '危险操作',
      {
        confirmButtonText: '确认注销',
        cancelButtonText: '取消',
        type: 'error',
      }
    )
    
    await ElMessageBox.prompt('请输入"确认注销"以继续', '再次确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'error',
      inputPattern: /^确认注销$/,
      inputErrorMessage: '请输入"确认注销"',
    })
    
    await userApi.deleteAccount()
    await userStore.logout()
    router.push('/login')
    ElMessage.success('账号已注销')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('注销失败:', error)
    }
  }
}

onMounted(() => {
  // 从 localStorage 恢复深色模式
  const savedDarkMode = localStorage.getItem('darkMode') === 'true'
  profileForm.value.darkMode = savedDarkMode
  document.documentElement.setAttribute('data-theme', savedDarkMode ? 'dark' : '')
  
  if (userInfo.value) {
    profileForm.value = {
      nickname: userInfo.value.nickname || '',
      avatar: userInfo.value.avatar || '',
      dailyGoal: userInfo.value.dailyGoal || 120,
      weeklyGoal: userInfo.value.weeklyGoal || 840,
      monthlyGoal: userInfo.value.monthlyGoal || 3600,
      darkMode: savedDarkMode,
      notificationEnabled: userInfo.value.notificationEnabled !== false,
    }
  }
})
</script>

<style scoped>
.settings-container {
  padding: 0;
}

.card-title {
  font-weight: 600;
  color: var(--text-primary);
}

.settings-form {
  max-width: 600px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border);
}

.profile-avatar {
  background: var(--primary);
}

.avatar-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.avatar-tip {
  font-size: 12px;
  color: var(--text-muted);
}

.save-btn {
  background: var(--primary);
  border-color: transparent;
}

.save-btn:hover {
  background: var(--primary-dark);
  border-color: transparent;
}

.account-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  width: 100%;
  height: 44px;
}

.setting-hint {
  margin-left: 8px;
  font-size: 12px;
  color: var(--text-muted);
}
</style>
