// 本地存储封装

export const storage = {
  // 获取
  get(key) {
    try {
      return uni.getStorageSync(key)
    } catch (e) {
      console.error('Storage get error:', e)
      return null
    }
  },

  // 设置
  set(key, value) {
    try {
      uni.setStorageSync(key, value)
    } catch (e) {
      console.error('Storage set error:', e)
    }
  },

  // 删除
  remove(key) {
    try {
      uni.removeStorageSync(key)
    } catch (e) {
      console.error('Storage remove error:', e)
    }
  },

  // 清空
  clear() {
    try {
      uni.clearStorageSync()
    } catch (e) {
      console.error('Storage clear error:', e)
    }
  }
}

export default storage
