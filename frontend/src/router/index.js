import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/landing',
    name: 'Landing',
    component: () => import('@/views/landing/LandingView.vue'),
    meta: { requiresAuth: false, title: '首页' },
  },
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
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPasswordView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/AdminView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
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
        path: 'efficiency',
        name: 'Efficiency',
        component: () => import('@/views/efficiency/EfficiencyView.vue'),
        meta: { title: '效率分析', icon: 'TrendCharts' },
      },
      {
        path: 'materials',
        name: 'Materials',
        component: () => import('@/views/materials/MaterialsView.vue'),
        meta: { title: '学习资料', icon: 'Folder' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/SettingsView.vue'),
        meta: { title: '设置', icon: 'Setting' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/landing',
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
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  
  // 不需要登录的页面
  const publicPages = ['/landing', '/login', '/register', '/forgot-password']
  const isPublicPage = publicPages.includes(to.path) || to.path.startsWith('/landing')
  
  if (!isPublicPage && !token) {
    // 需要登录但没有token，跳转到登录页
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    // 已登录但访问登录/注册页，跳转到首页
    next('/home')
  } else if (to.path === '/' && !token) {
    // 访问根路径但没有token，跳转到landing
    next('/landing')
  } else if (to.meta.requiresAdmin && userInfo.role !== 'admin') {
    // 需要管理员权限但不是管理员，跳转到首页
    next('/home')
  } else {
    next()
  }
})

router.afterEach((to) => {
  const title = to.meta.title ? `${to.meta.title} - 智学伴` : '智学伴'
  document.title = title
})

export default router
