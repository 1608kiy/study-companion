import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useStudyStore } from '@/stores/study'

describe('useStudyStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('初始计时器状态未运行', () => {
    const store = useStudyStore()
    expect(store.isTimerRunning).toBe(false)
    expect(store.timerState.isRunning).toBe(false)
  })

  it('elapsedMinutes 正确计算', () => {
    const store = useStudyStore()
    store.timerState.elapsedSeconds = 90
    expect(store.elapsedMinutes).toBe(1)
  })

  it('elapsedSecondsDisplay 返回秒数余数', () => {
    const store = useStudyStore()
    store.timerState.elapsedSeconds = 125
    expect(store.elapsedSecondsDisplay).toBe(5)
  })

  it('elapsedSecondsDisplay 零秒返回0', () => {
    const store = useStudyStore()
    store.timerState.elapsedSeconds = 60
    expect(store.elapsedSecondsDisplay).toBe(0)
  })
})
