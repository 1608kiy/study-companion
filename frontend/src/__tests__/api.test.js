import { describe, it, expect } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

describe('API 模块导出', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('authApi 包含 login 方法', async () => {
    const { authApi } = await import('@/api/modules')
    expect(typeof authApi.login).toBe('function')
  })

  it('authApi 包含 register 方法', async () => {
    const { authApi } = await import('@/api/modules')
    expect(typeof authApi.register).toBe('function')
  })

  it('userApi 包含 getProfile 方法', async () => {
    const { userApi } = await import('@/api/modules')
    expect(typeof userApi.getProfile).toBe('function')
  })

  it('subjectApi 包含 getList 方法', async () => {
    const { subjectApi } = await import('@/api/modules')
    expect(typeof subjectApi.getList).toBe('function')
  })

  it('studyRecordApi 包含 getStats 方法', async () => {
    const { studyRecordApi } = await import('@/api/modules')
    expect(typeof studyRecordApi.getStats).toBe('function')
  })

  it('diaryApi 包含 create 方法', async () => {
    const { diaryApi } = await import('@/api/modules')
    expect(typeof diaryApi.create).toBe('function')
  })

  it('goalApi 包含 getMonthlyStats 方法', async () => {
    const { goalApi } = await import('@/api/modules')
    expect(typeof goalApi.getMonthlyStats).toBe('function')
  })

  it('aiApi 包含 chat 方法', async () => {
    const { aiApi } = await import('@/api/modules')
    expect(typeof aiApi.chat).toBe('function')
  })

  it('checkInApi 包含 checkIn 方法', async () => {
    const { checkInApi } = await import('@/api/modules')
    expect(typeof checkInApi.checkIn).toBe('function')
  })
})
