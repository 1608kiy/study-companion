import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('初始状态 token 为空', () => {
    const store = useUserStore()
    expect(store.token).toBe('')
  })

  it('setToken 存储 token', () => {
    const store = useUserStore()
    store.setToken('test-token-123')
    expect(store.token).toBe('test-token-123')
    expect(localStorage.getItem('token')).toBe('test-token-123')
  })

  it('clearToken 清除 token', () => {
    const store = useUserStore()
    store.setToken('test-token')
    store.clearToken()
    expect(store.token).toBe('')
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('logout 清除所有状态', () => {
    const store = useUserStore()
    store.setToken('test-token')
    store.clearToken()
    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
  })
})
