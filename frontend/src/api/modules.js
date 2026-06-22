import api from './index'

// 认证相关API
export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  forgotPassword: (data) => api.post('/auth/forgot-password', data),
  resetPassword: (data) => api.post('/auth/reset-password', data),
}

// 用户相关API
export const userApi = {
  getProfile: () => api.get('/user/profile'),
  updateProfile: (data) => api.put('/user/profile', data),
  logout: () => api.post('/user/logout'),
  deleteAccount: () => api.delete('/user/delete'),
}

// 科目相关API
export const subjectApi = {
  getList: () => api.get('/subjects'),
  getPreset: () => api.get('/subjects/preset'),
  create: (data) => api.post('/subjects', data),
  update: (id, data) => api.put(`/subjects/${id}`, data),
  delete: (id) => api.delete(`/subjects/${id}`),
}

// 学习记录相关API
export const studyRecordApi = {
  startTimer: (data) => api.post('/study-records/timer/start', data),
  pauseTimer: () => api.post('/study-records/timer/pause'),
  resumeTimer: () => api.post('/study-records/timer/resume'),
  stopTimer: () => api.post('/study-records/timer/stop'),
  getTimerState: () => api.get('/study-records/timer/state'),
  getList: (params) => api.get('/study-records', { params }),
  getPagedList: (params) => api.get('/study-records/paged', { params }),
  getById: (id) => api.get(`/study-records/${id}`),
  aiJudge: (recordId, reason) => api.post(`/study-records/${recordId}/ai-judge`, { reason }),
  update: (id, data) => api.put(`/study-records/${id}`, data),
  getStats: () => api.get('/study-records/stats'),
  getEfficiency: () => api.get('/study-records/efficiency'),
}

// 打卡相关API
export const checkInApi = {
  getTodayStatus: () => api.get('/check-in/today'),
  checkIn: () => api.post('/check-in'),
  getHistory: (params) => api.get('/check-in/history', { params }),
  recordMiss: (data) => api.post('/check-in/miss', data),
  aiJudge: (missRecordId) => api.post(`/check-in/miss/${missRecordId}/ai-judge`),
  replenish: (missRecordId) => api.post(`/check-in/miss/${missRecordId}/replenish`),
  getMissRecords: () => api.get('/check-in/miss'),
}

// 日记相关API
export const diaryApi = {
  getList: (params) => api.get('/diaries', { params }),
  getPagedList: (params) => api.get('/diaries/paged', { params }),
  getByDate: (date) => api.get(`/diaries/date/${date}`),
  create: (data) => api.post('/diaries', data),
  generate: () => api.post('/diaries/generate'),
  regenerate: (id) => api.post(`/diaries/${id}/regenerate`),
  uploadImage: (diaryId, file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post(`/diaries/${diaryId}/images`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  deleteImage: (imageId) => api.delete(`/diaries/images/${imageId}`),
  getImages: (diaryId) => api.get(`/diaries/${diaryId}/images`),
}

// 目标相关API
export const goalApi = {
  getList: (params) => api.get('/goals', { params }),
  create: (data) => api.post('/goals', data),
  update: (id, data) => api.put(`/goals/${id}`, data),
  delete: (id) => api.delete(`/goals/${id}`),
  aiSuggest: (goalType) => api.post(`/goals/ai-suggest?goalType=${goalType}`),
  getDailyStats: (params) => api.get('/goals/stats/daily', { params }),
  getWeeklyStats: (params) => api.get('/goals/stats/weekly', { params }),
  getMonthlyStats: (params) => api.get('/goals/stats/monthly', { params }),
  getCalendarStats: (params) => api.get('/goals/stats/calendar', { params }),
}

// AI相关API
export const aiApi = {
  generateWeeklyReport: () => api.post('/ai/weekly-report'),
  generateMonthlyReport: () => api.post('/ai/monthly-report'),
  chat: (data) => api.post('/ai/chat', data),
  judgeFocus: (studyContext) => api.post('/ai/focus-judge', { studyContext }),
  getShareImage: () => api.get('/ai/share'),
  getChatHistory: (limit = 20) => api.get('/ai/chat/history', { params: { limit } }),
  clearChatHistory: () => api.delete('/ai/chat/history'),
}

// 考试相关API
export const examApi = {
  getList: () => api.get('/exams'),
  create: (data) => api.post('/exams', data),
  update: (id, data) => api.put(`/exams/${id}`, data),
  delete: (id) => api.delete(`/exams/${id}`),
}

// 学习资料相关API
export const materialApi = {
  getList: (params) => api.get('/materials', { params }),
  getById: (id) => api.get(`/materials/${id}`),
  upload: (formData) => api.post('/materials', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  update: (id, data) => api.put(`/materials/${id}`, data),
  delete: (id) => api.delete(`/materials/${id}`),
  toggleFavorite: (id) => api.post(`/materials/${id}/favorite`),
}

// 管理员相关API
export const adminApi = {
  getDashboard: () => api.get('/admin/dashboard'),
  getUsers: (params) => api.get('/admin/users', { params }),
  updateUser: (id, data) => api.put(`/admin/users/${id}`, data),
  deleteUser: (id) => api.delete(`/admin/users/${id}`),
  getAiConfig: () => api.get('/admin/ai/config'),
  updateAiConfig: (data) => api.put('/admin/ai/config', data),
}
