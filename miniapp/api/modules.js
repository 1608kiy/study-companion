// API 端点函数
import { get, post, put, del } from './index'

// 认证相关API
export const authApi = {
  register: (data) => post('/auth/register', data),
  login: (data) => post('/auth/login', data)
}

// 用户相关API
export const userApi = {
  getProfile: () => get('/user/profile'),
  updateProfile: (data) => put('/user/profile', data),
  logout: () => post('/user/logout'),
  deleteAccount: () => del('/user/delete')
}

// 科目相关API
export const subjectApi = {
  getList: () => get('/subjects'),
  getPreset: () => get('/subjects/preset'),
  create: (data) => post('/subjects', data),
  update: (id, data) => put(`/subjects/${id}`, data),
  delete: (id) => del(`/subjects/${id}`)
}

// 学习记录相关API
export const studyRecordApi = {
  startTimer: (data) => post('/study-records/timer/start', data),
  pauseTimer: () => post('/study-records/timer/pause'),
  resumeTimer: () => post('/study-records/timer/resume'),
  stopTimer: () => post('/study-records/timer/stop'),
  getTimerState: () => get('/study-records/timer/state'),
  getList: (params) => get('/study-records', params),
  getPagedList: (params) => get('/study-records/paged', params),
  getById: (id) => get(`/study-records/${id}`),
  update: (id, data) => put(`/study-records/${id}`, data),
  delete: (id) => del(`/study-records/${id}`),
  getStats: () => get('/study-records/stats')
}

// 打卡相关API
export const checkInApi = {
  getTodayStatus: () => get('/check-in/today'),
  checkIn: () => post('/check-in'),
  getHistory: (params) => get('/check-in/history', params),
  recordMiss: (data) => post('/check-in/miss', data),
  aiJudge: (missRecordId) => post(`/check-in/miss/${missRecordId}/ai-judge`),
  replenish: (missRecordId) => post(`/check-in/miss/${missRecordId}/replenish`),
  getMissRecords: () => get('/check-in/miss')
}

// 日记相关API
export const diaryApi = {
  getList: (params) => get('/diaries', params),
  getPagedList: (params) => get('/diaries/paged', params),
  getByDate: (date) => get(`/diaries/date/${date}`),
  create: (data) => post('/diaries', data),
  update: (id, data) => put(`/diaries/${id}`, data),
  delete: (id) => del(`/diaries/${id}`),
  generate: () => post('/diaries/generate'),
  regenerate: (id) => post(`/diaries/${id}/regenerate`)
}

// 目标相关API
export const goalApi = {
  getList: (params) => get('/goals', params),
  create: (data) => post('/goals', data),
  update: (id, data) => put(`/goals/${id}`, data),
  delete: (id) => del(`/goals/${id}`),
  aiSuggest: (goalType) => post(`/goals/ai-suggest?goalType=${goalType}`),
  getDailyStats: (params) => get('/goals/stats/daily', params),
  getWeeklyStats: (params) => get('/goals/stats/weekly', params),
  getMonthlyStats: (params) => get('/goals/stats/monthly', params),
  getCalendarStats: (params) => get('/goals/stats/calendar', params)
}

// AI相关API
export const aiApi = {
  generateWeeklyReport: () => post('/ai/weekly-report'),
  generateMonthlyReport: () => post('/ai/monthly-report'),
  chat: (data) => post('/ai/chat', data),
  judgeFocus: (studyContext) => post('/ai/focus-judge', { studyContext }),
  getShareImage: () => get('/ai/share')
}
