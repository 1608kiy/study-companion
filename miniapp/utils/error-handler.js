// 全局错误处理

// 错误上报（可扩展为发送到服务器）
const reportError = (error, info) => {
  console.error('[App Error]', info, error)
  
  // 可以在这里添加错误上报逻辑
  // 例如：发送到后端日志服务
}

// Vue 错误处理
export const onVueError = (err, vm, info) => {
  reportError(err, `Vue Error: ${info}`)
  
  // 防止白屏，显示友好提示
  uni.showToast({
    title: '页面出现异常，请重试',
    icon: 'none',
    duration: 2000
  })
}

// Promise 未捕获错误
export const onUnhandledRejection = (reason) => {
  reportError(reason, 'Unhandled Promise Rejection')
  
  // 不显示 toast，避免频繁弹窗
  console.warn('[Unhandled Rejection]', reason)
}

// 全局错误
export const onGlobalError = (message, source, lineno, colno, error) => {
  reportError(error, `Global Error: ${message}`)
}

// 页面不存在处理
export const onPageNotFound = (res) => {
  console.warn('[Page Not Found]', res.path)
  uni.switchTab({ url: '/pages/home/home' })
}

// 初始化错误处理
export const initErrorHandler = (app) => {
  // Vue 错误
  app.config.errorHandler = onVueError
  
  // Promise 错误
  // #ifdef H5
  window.addEventListener('unhandledrejection', onUnhandledRejection)
  window.addEventListener('error', onGlobalError)
  // #endif
  
  // 小程序环境
  // #ifdef MP
  uni.onPageNotFound(onPageNotFound)
  // #endif
}

export default {
  initErrorHandler,
  onVueError,
  onUnhandledRejection,
  onGlobalError,
  onPageNotFound
}
