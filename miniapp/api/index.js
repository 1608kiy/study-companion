// API 请求封装
import { getConfig } from '../config'

const TIMEOUT = 10000

// 请求拦截器
const requestInterceptor = (config) => {
  const token = uni.getStorageSync('token')
  if (token) {
    config.header = {
      ...config.header,
      'Authorization': `Bearer ${token}`
    }
  }
  return config
}

// 响应拦截器
const responseInterceptor = (response) => {
  const { statusCode, data } = response

  // HTTP 状态码处理
  if (statusCode === 401) {
    uni.removeStorageSync('token')
    uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/login/login' })
    }, 1500)
    return Promise.reject(new Error('未授权'))
  }

  if (statusCode !== 200) {
    uni.showToast({ title: data?.message || '请求失败', icon: 'none' })
    return Promise.reject(new Error(data?.message || '请求失败'))
  }

  // 业务状态码处理
  if (data.code !== 200) {
    uni.showToast({ title: data.message || '操作失败', icon: 'none' })
    if (data.code === 401) {
      uni.removeStorageSync('token')
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/login/login' })
      }, 1500)
    }
    return Promise.reject(new Error(data.message || '操作失败'))
  }

  return data
}

// 封装 uni.request
const request = (options) => {
  const { baseUrl } = getConfig()
  
  const config = {
    url: `${baseUrl}${options.url}`,
    method: options.method || 'GET',
    data: options.data || {},
    header: {
      'Content-Type': 'application/json',
      ...options.header
    },
    timeout: TIMEOUT
  }

  // 应用请求拦截器
  const interceptedConfig = requestInterceptor(config)

  return new Promise((resolve, reject) => {
    uni.request({
      ...interceptedConfig,
      success: (response) => {
        responseInterceptor(response).then(resolve).catch(reject)
      },
      fail: (error) => {
        uni.showToast({ title: '网络错误，请检查网络连接', icon: 'none' })
        reject(error)
      }
    })
  })
}

// 导出请求方法
export const get = (url, params) => {
  const query = params ? '?' + Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null)
    .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
    .join('&') : ''
  return request({ url: `${url}${query}`, method: 'GET' })
}

export const post = (url, data) => {
  return request({ url, method: 'POST', data })
}

export const put = (url, data) => {
  return request({ url, method: 'PUT', data })
}

export const del = (url, data) => {
  return request({ url, method: 'DELETE', data })
}

export default { get, post, put, del }
