<template>
  <el-container class="main-layout">
    <!-- 移动端遮罩 -->
    <div v-if="mobileOpen" class="mobile-overlay" @click="mobileOpen = false" />
    <el-aside :width="asideWidth" class="sidebar" :class="{ 'mobile-open': mobileOpen }">
      <div class="sidebar-logo">
        <img src="@/assets/logo.svg" alt="智学伴 Logo" />
        <span v-if="!isCollapse" class="logo-text">智学伴</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        class="sidebar-menu"
        @select="mobileOpen = false"
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const mobileOpen = ref(false)
const isMobile = ref(window.innerWidth < 768)
const userInfo = computed(() => userStore.userInfo)

const asideWidth = computed(() => {
  if (isMobile.value) return '220px'
  return isCollapse.value ? '64px' : '220px'
})

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) mobileOpen.value = false
}

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
  if (isMobile.value) {
    mobileOpen.value = !mobileOpen.value
  } else {
    isCollapse.value = !isCollapse.value
  }
}

const handleCommand = async (command) => {
  if (command === 'settings') {
    router.push('/settings')
  } else if (command === 'logout') {
    try {
      await userStore.logout()
      router.push('/login')
    } catch (error) {
      ElMessage.error('退出失败')
    }
  }
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  if (!userStore.userInfo) {
    try {
      await userStore.getProfile()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.sidebar {
  background: var(--sidebar-bg);
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
  border-bottom: 1px solid var(--border);
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
  color: var(--text-secondary);
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: var(--text-primary);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
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
  background-color: var(--bg-page);
}

.user-avatar {
  background: var(--primary);
}

.username {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 14px;
}

.user-arrow {
  color: var(--text-muted);
  font-size: 12px;
}

.main-content {
  background-color: var(--bg-page);
  padding: 24px;
  min-height: calc(100vh - 64px);
}

:deep(.el-menu) {
  background: transparent;
}

:deep(.el-menu-item) {
  color: var(--text-muted);
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
  background: var(--primary);
  color: white;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 767px) {
  .sidebar {
    position: fixed;
    left: -220px;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transition: left 0.3s ease;
  }
  .sidebar.mobile-open {
    left: 0;
  }
  .mobile-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 999;
  }
  .main-content {
    padding: 16px;
  }
  .header {
    padding: 0 16px;
  }
  .page-title {
    font-size: 16px;
  }
  .username {
    display: none;
  }
}
</style>
