import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock API modules
vi.mock('@/api/modules', () => ({
  studyRecordApi: {
    startTimer: vi.fn(),
    pauseTimer: vi.fn(),
    resumeTimer: vi.fn(),
    stopTimer: vi.fn(),
    getTimerState: vi.fn(),
    getList: vi.fn(),
    getStats: vi.fn(),
    delete: vi.fn(),
  },
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
  },
  userApi: {
    getProfile: vi.fn(),
    updateProfile: vi.fn(),
    logout: vi.fn(),
    deleteAccount: vi.fn(),
  },
}))

import { useStudyStore } from '@/stores/study'
import { useUserStore } from '@/stores/user'
import { studyRecordApi, authApi, userApi } from '@/api/modules'

describe('StudyStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('初始状态正确', () => {
    const store = useStudyStore()
    expect(store.isTimerRunning).toBe(false)
    expect(store.isPaused).toBe(false)
    expect(store.timerState.elapsedSeconds).toBe(0)
    expect(store.timerState.subjectId).toBeNull()
  })

  it('startTimer 成功后状态更新', async () => {
    const store = useStudyStore()
    studyRecordApi.startTimer.mockResolvedValue({
      code: 200,
      data: { isRunning: true, subjectId: 1, subjectName: '行测', elapsedSeconds: 0 },
    })

    await store.startTimer(1)

    expect(store.isTimerRunning).toBe(true)
    expect(store.timerState.subjectId).toBe(1)
    expect(store.timerState.subjectName).toBe('行测')
  })

  it('pauseTimer 成功后状态更新', async () => {
    const store = useStudyStore()
    // 先启动计时器
    store.timerState = { isRunning: true, subjectId: 1, subjectName: '行测', elapsedSeconds: 60 }
    studyRecordApi.pauseTimer.mockResolvedValue({
      code: 200,
      data: { isRunning: false, subjectId: 1, subjectName: '行测', elapsedSeconds: 60 },
    })

    await store.pauseTimer()

    expect(store.isTimerRunning).toBe(false)
    expect(store.isPaused).toBe(true)
  })

  it('stopTimer 成功后状态重置', async () => {
    const store = useStudyStore()
    store.timerState = { isRunning: true, subjectId: 1, subjectName: '行测', elapsedSeconds: 120 }
    studyRecordApi.stopTimer.mockResolvedValue({
      code: 200,
      data: { id: 1, duration: 2 },
    })

    const res = await store.stopTimer()

    expect(store.isTimerRunning).toBe(false)
    expect(store.timerState.subjectId).toBeNull()
    expect(store.timerState.elapsedSeconds).toBe(0)
    expect(res.data.id).toBe(1)
  })

  it('incrementElapsed 增加计时', () => {
    const store = useStudyStore()
    store.timerState.elapsedSeconds = 10
    store.incrementElapsed()
    expect(store.timerState.elapsedSeconds).toBe(11)
  })

  it('isPaused 计算属性正确', () => {
    const store = useStudyStore()
    // 未启动
    expect(store.isPaused).toBe(false)

    // 运行中
    store.timerState = { isRunning: true, subjectId: 1 }
    expect(store.isPaused).toBe(false)

    // 暂停中
    store.timerState = { isRunning: false, subjectId: 1 }
    expect(store.isPaused).toBe(true)

    // 停止后
    store.timerState = { isRunning: false, subjectId: null }
    expect(store.isPaused).toBe(false)
  })
})

describe('UserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('login 成功后保存 token', async () => {
    const store = useUserStore()
    authApi.login.mockResolvedValue({
      code: 200,
      data: { token: 'test-token', userId: 1, email: 'test@test.com', nickname: 'test' },
    })

    await store.login({ email: 'test@test.com', password: '123456' })

    expect(store.token).toBe('test-token')
    expect(store.userInfo.nickname).toBe('test')
    expect(localStorage.getItem('token')).toBe('test-token')
  })

  it('logout 清除所有状态', async () => {
    const store = useUserStore()
    store.setToken('test-token')
    store.userInfo = { nickname: 'test' }
    userApi.logout.mockResolvedValue({ code: 200 })

    await store.logout()

    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('getProfile 成功后更新 userInfo', async () => {
    const store = useUserStore()
    userApi.getProfile.mockResolvedValue({
      code: 200,
      data: { id: 1, email: 'test@test.com', nickname: 'test', dailyGoal: 120 },
    })

    await store.getProfile()

    expect(store.userInfo.nickname).toBe('test')
    expect(store.userInfo.dailyGoal).toBe(120)
  })

  it('setToken 同步写入 state 和 localStorage', () => {
    const store = useUserStore()
    store.setToken('new-token')
    expect(store.token).toBe('new-token')
    expect(localStorage.getItem('token')).toBe('new-token')
  })

  it('clearToken 同步清除 state 和 localStorage', () => {
    const store = useUserStore()
    store.setToken('token')
    store.clearToken()
    expect(store.token).toBe('')
    expect(localStorage.getItem('token')).toBeNull()
  })
})
