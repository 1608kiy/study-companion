import { describe, it, expect } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import { routes } from '@/router/index.js'

describe('路由配置', () => {
  it('包含登录路由', () => {
    const loginRoute = routes.find(r => r.path === '/login')
    expect(loginRoute).toBeDefined()
  })

  it('包含注册路由', () => {
    const registerRoute = routes.find(r => r.path === '/register')
    expect(registerRoute).toBeDefined()
  })

  it('包含主布局路由', () => {
    const rootRoute = routes.find(r => r.path === '/')
    expect(rootRoute).toBeDefined()
    expect(rootRoute.children).toBeDefined()
  })

  it('子路由包含首页', () => {
    const rootRoute = routes.find(r => r.path === '/')
    const homeRoute = rootRoute.children.find(r => r.path === 'home')
    expect(homeRoute).toBeDefined()
  })

  it('子路由包含学习页', () => {
    const rootRoute = routes.find(r => r.path === '/')
    const studyRoute = rootRoute.children.find(r => r.path === 'study')
    expect(studyRoute).toBeDefined()
  })

  it('子路由包含日记页', () => {
    const rootRoute = routes.find(r => r.path === '/')
    const diaryRoute = rootRoute.children.find(r => r.path === 'diary')
    expect(diaryRoute).toBeDefined()
  })

  it('子路由包含统计页', () => {
    const rootRoute = routes.find(r => r.path === '/')
    const statsRoute = rootRoute.children.find(r => r.path === 'stats')
    expect(statsRoute).toBeDefined()
  })

  it('子路由包含AI助手页', () => {
    const rootRoute = routes.find(r => r.path === '/')
    const aiRoute = rootRoute.children.find(r => r.path === 'ai')
    expect(aiRoute).toBeDefined()
  })

  it('子路由包含设置页', () => {
    const rootRoute = routes.find(r => r.path === '/')
    const settingsRoute = rootRoute.children.find(r => r.path === 'settings')
    expect(settingsRoute).toBeDefined()
  })
})
