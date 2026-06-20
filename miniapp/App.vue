<script>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from './store/user'

export default {
  onLaunch() {
    console.log('App Launch')
    const userStore = useUserStore()
    // 恢复登录状态
    const token = uni.getStorageSync('token')
    if (token) {
      userStore.setToken(token)
      userStore.getProfile().catch(err => {
        console.error('获取用户信息失败:', err)
        // Token 可能过期，清除
        userStore.clearToken()
      })
    }
  },
  onShow() {
    console.log('App Show')
    // 隐藏原生 tabBar（使用自定义组件）
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
