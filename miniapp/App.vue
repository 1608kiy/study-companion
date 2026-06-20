<script>
import { onLaunch } from '@dcloudio/uni-app'
import { useUserStore } from './store/user'

export default {
  onLaunch() {
    console.log('App Launch')
    const userStore = useUserStore()
    // 恢复登录状态
    const token = uni.getStorageSync('token')
    if (token) {
      userStore.setToken(token)
      userStore.getProfile()
    }
  },
  onShow() {
    console.log('App Show')
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
</style>
