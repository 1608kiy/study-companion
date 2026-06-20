<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="sidebar-logo">
        <img src="@/assets/logo.svg" alt="Logo" />
        <span v-if="!isCollapse" class="logo-text">智学伴</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        class="sidebar-menu"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ currentPageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="34" icon="User" class="user-avatar" />
              <span class="username">{{ userInfo?.nickname || '用户' }}</span>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  设置
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const userInfo = computed(() => userStore.userInfo)

const menuItems = [
  { path: '/home', title: '首页', icon: 'House' },
  { path: '/study', title: '学习', icon: 'Timer' },
  { path: '/diary', title: '日记', icon: 'Notebook' },
  { path: '/stats', title: '统计', icon: 'DataAnalysis' },
  { path: '/ai', title: 'AI助手', icon: 'ChatDotRound' },
  { path: '/settings', title: '设置', icon: 'Setting' },
]

const activeMenu = computed(() => route.path)
const currentPageTitle = computed(() => {
  const item = menuItems.find(item => item.path === route.path)
  return item?.title || '智学伴'
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'settings') {
    router.push('/settings')
  } else if (command === 'logout') {
    await userStore.logout()
    router.push('/login')
  }
}

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.getProfile()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  transition: width 0.3s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-logo img {
  width: 32px;
  height: 32px;
}

.logo-text {
  color: white;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
  padding: 8px 0;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.header {
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #64748b;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #1e293b;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f1f5f9;
}

.user-avatar {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

.username {
  color: #1e293b;
  font-weight: 500;
  font-size: 14px;
}

.user-arrow {
  color: #94a3b8;
  font-size: 12px;
}

.main-content {
  background-color: #f8fafc;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

:deep(.el-menu) {
  background: transparent;
}

:deep(.el-menu-item) {
  color: #94a3b8;
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 8px;
  transition: all 0.2s;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.08);
  color: white;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  font-weight: 500;
}
</style>
