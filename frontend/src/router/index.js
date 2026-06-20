import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/HomeView.vue'),
        meta: { title: '首页', icon: 'House' },
      },
      {
        path: 'study',
        name: 'Study',
        component: () => import('@/views/study/StudyView.vue'),
        meta: { title: '学习', icon: 'Timer' },
      },
      {
        path: 'diary',
        name: 'Diary',
        component: () => import('@/views/diary/DiaryView.vue'),
        meta: { title: '日记', icon: 'Notebook' },
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('@/views/stats/StatsView.vue'),
        meta: { title: '统计', icon: 'DataAnalysis' },
      },
      {
        path: 'ai',
        name: 'AiAssistant',
        component: () => import('@/views/ai/AiView.vue'),
        meta: { title: 'AI助手', icon: 'ChatDotRound' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/SettingsView.vue'),
        meta: { title: '设置', icon: 'Setting' },
      },
    ],
  },
]

export { routes }

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/')
  } else {
    next()
  }
})

export default router
