<template>
  <el-container class="main-layout">
    <!-- 移动端遮罩 -->
    <div v-if="mobileOpen" class="mobile-overlay" @click="mobileOpen = false" />
    <el-aside v-if="!isMobile" :width="asideWidth" class="sidebar">
      <div class="sidebar-logo">
        <img src="@/assets/logo.svg" alt="智学伴 Logo" />
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
    
    <!-- 移动端侧边栏抽屉 -->
    <el-aside v-else width="220px" class="sidebar mobile-drawer" :class="{ 'mobile-open': mobileOpen }">
      <div class="sidebar-logo">
        <img src="@/assets/logo.svg" alt="智学伴 Logo" />
        <span class="logo-text">智学伴</span>
      </div>
      <el-menu
        :default-active="activeMenu"
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
          <el-icon v-if="isMobile" class="menu-btn" @click="mobileOpen = !mobileOpen">
            <Menu />
          </el-icon>
          <el-icon v-else class="collapse-btn" @click="isCollapse = !isCollapse">
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
      
      <el-main class="main-content" :class="{ 'has-bottom-nav': isMobile }">
        <router-view />
      </el-main>
    </el-container>
    
    <!-- 移动端底部导航栏 -->
    <div v-if="isMobile" class="bottom-nav">
      <router-link 
        v-for="item in bottomNavItems" 
        :key="item.path" 
        :to="item.path" 
        class="bottom-nav-item"
        :class="{ active: activeMenu === item.path }"
      >
        <el-icon size="20"><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </router-link>
    </div>
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

const bottomNavItems = [
  { path: '/home', title: '首页', icon: 'House' },
  { path: '/study', title: '学习', icon: 'Timer' },
  { path: '/diary', title: '日记', icon: 'Notebook' },
  { path: '/stats', title: '统计', icon: 'DataAnalysis' },
  { path: '/ai', title: 'AI', icon: 'ChatDotRound' },
]

const activeMenu = computed(() => route.path)
const currentPageTitle = computed(() => {
  const item = menuItems.find(item => item.path === route.path)
  return item?.title || '智学伴'
})

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
  
  // 初始化深色模式
  const savedDarkMode = localStorage.getItem('darkMode') === 'true'
  document.documentElement.setAttribute('data-theme', savedDarkMode ? 'dark' : '')
  
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
  background: var(--bg-card);
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

.main-content.has-bottom-nav {
  padding-bottom: 70px;
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

/* 移动端侧边栏抽屉 */
.mobile-drawer {
  position: fixed;
  left: -220px;
  top: 0;
  bottom: 0;
  z-index: 1000;
  transition: left 0.3s ease;
}

.mobile-drawer.mobile-open {
  left: 0;
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.menu-btn {
  font-size: 22px;
  cursor: pointer;
  color: var(--text-secondary);
}

/* 底部导航栏 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: var(--bg-card);
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.bottom-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 12px;
  text-decoration: none;
  color: var(--text-muted);
  font-size: 11px;
  transition: color 0.2s;
}

.bottom-nav-item.active {
  color: var(--primary);
}

/* 响应式 */
@media (max-width: 767px) {
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
