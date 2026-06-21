import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock localStorage
const localStorageMock = (() => {
  let store = {}
  return {
    getItem: vi.fn(key => store[key] || null),
    setItem: vi.fn((key, value) => { store[key] = value }),
    removeItem: vi.fn(key => { delete store[key] }),
    clear: vi.fn(() => { store = {} }),
  }
})()
vi.stubGlobal('localStorage', localStorageMock)

describe('路由守卫逻辑', () => {
  // 模拟守卫逻辑（与 router/index.js 中一致）
  function simulateGuard(toPath, requiresAuth) {
    const token = localStorage.getItem('token')
    if (requiresAuth && !token) {
      return '/login'
    } else if ((toPath === '/login' || toPath === '/register') && token) {
      return '/'
    }
    return toPath
  }

  beforeEach(() => {
    localStorageMock.clear()
  })

  it('未登录时访问需要认证的页面应跳转到 /login', () => {
    const result = simulateGuard('/home', true)
    expect(result).toBe('/login')
  })

  it('未登录时访问子路由应跳转到 /login', () => {
    const result = simulateGuard('/study', true)
    expect(result).toBe('/login')
  })

  it('已登录时访问登录页应跳转到首页', () => {
    localStorageMock.setItem('token', 'test-token')
    const result = simulateGuard('/login', false)
    expect(result).toBe('/')
  })

  it('已登录时访问注册页应跳转到首页', () => {
    localStorageMock.setItem('token', 'test-token')
    const result = simulateGuard('/register', false)
    expect(result).toBe('/')
  })

  it('已登录时访问需要认证的页面应正常进入', () => {
    localStorageMock.setItem('token', 'test-token')
    const result = simulateGuard('/home', true)
    expect(result).toBe('/home')
  })

  it('未登录时访问公开页面应正常进入', () => {
    const result = simulateGuard('/login', false)
    expect(result).toBe('/login')
  })
})

describe('路由配置', () => {
  it('父路由 requiresAuth 为 true', async () => {
    const { routes } = await import('@/router/index.js')
    const rootRoute = routes.find(r => r.path === '/')
    expect(rootRoute.meta.requiresAuth).toBe(true)
  })

  it('登录路由 requiresAuth 为 false', async () => {
    const { routes } = await import('@/router/index.js')
    const loginRoute = routes.find(r => r.path === '/login')
    expect(loginRoute.meta.requiresAuth).toBe(false)
  })

  it('注册路由 requiresAuth 为 false', async () => {
    const { routes } = await import('@/router/index.js')
    const registerRoute = routes.find(r => r.path === '/register')
    expect(registerRoute.meta.requiresAuth).toBe(false)
  })

  it('子路由包含 8 个页面', async () => {
    const { routes } = await import('@/router/index.js')
    const rootRoute = routes.find(r => r.path === '/')
    expect(rootRoute.children).toHaveLength(8)
  })

  it('存在 404 兜底路由', async () => {
    const { routes } = await import('@/router/index.js')
    const catchAll = routes.find(r => r.path === '/:pathMatch(.*)*')
    expect(catchAll).toBeDefined()
    expect(catchAll.redirect).toBe('/home')
  })
})

describe('页面标题', () => {
  it('子路由 meta 包含 title 字段', async () => {
    const { routes } = await import('@/router/index.js')
    const rootRoute = routes.find(r => r.path === '/')
    const titles = rootRoute.children.map(r => r.meta?.title)
    expect(titles).toContain('首页')
    expect(titles).toContain('学习')
    expect(titles).toContain('日记')
    expect(titles).toContain('统计')
    expect(titles).toContain('AI助手')
    expect(titles).toContain('设置')
  })
})
