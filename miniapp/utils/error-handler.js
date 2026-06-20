// 全局错误处理

// 错误上报（可扩展为发送到服务器）
const reportError = (error, info) => {
  console.error('[App Error]', info, error)
}

// Vue 错误处理
export const onVueError = (err, vm, info) => {
  reportError(err, `Vue Error: ${info}`)
  uni.showToast({
    title: '页面出现异常，请重试',
    icon: 'none',
    duration: 2000
  })
}

// Promise 未捕获错误
export const onUnhandledRejection = (reason) => {
  reportError(reason, 'Unhandled Promise Rejection')
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

// 初始化错误处理（传入 app 实例，不是组件实例）
export const initErrorHandler = (app) => {
  // Vue 错误 - app.config 只在 app 实例上存在
  if (app && app.config) {
    app.config.errorHandler = onVueError
  }
  
  // H5 环境
  // #ifdef H5
  window.addEventListener('unhandledrejection', onUnhandledRejection)
  window.addEventListener('error', onGlobalError)
  // #endif
  
  // 小程序环境
  // #ifdef MP
  uni.onPageNotFound(onPageNotFound)
  uni.onUnhandledRejection(onUnhandledRejection)
  // #endif
}

export default {
  initErrorHandler,
  onVueError,
  onUnhandledRejection,
  onGlobalError,
  onPageNotFound
}
