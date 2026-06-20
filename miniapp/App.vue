<script>
import { useUserStore } from './store/user'
import { initErrorHandler } from './utils/error-handler'
import { initVersionCheck } from './utils/version'

export default {
  onLaunch() {
    console.log('App Launch')
    
    // 初始化全局错误处理（传入 app 实例通过 getApp() 获取）
    // 注意：在 onLaunch 中，this.$getAppWebview() 或 getApp() 可用
    // 但最简单的方式是直接设置错误处理器
    initErrorHandler(getApp())
    
    // 初始化版本检查
    initVersionCheck()
    
    const userStore = useUserStore()
    // 恢复登录状态
    const token = uni.getStorageSync('token')
    if (token) {
      userStore.setToken(token)
      userStore.getProfile().catch(err => {
        console.error('获取用户信息失败:', err)
        userStore.clearToken()
      })
    }
  },
  onShow() {
    console.log('App Show')
    uni.hideTabBar()
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style>
@import './uni.scss';

page {
  background-color: #f8fafc;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* 隐藏原生 tabBar */
.uni-tabbar {
  display: none !important;
}
</style>
