// 版本更新检查

const VERSION_KEY = 'app_version'
const CURRENT_VERSION = '1.0.0'

// 检查并提示更新
export const checkUpdate = () => {
  // #ifdef MP-WEIXIN
  const updateManager = uni.getUpdateManager()
  
  updateManager.onCheckForUpdate((res) => {
    console.log('[Update] 检查更新:', res.hasUpdate)
  })
  
  updateManager.onUpdateReady(() => {
    uni.showModal({
      title: '更新提示',
      content: '新版本已经准备好，是否重启应用？',
      success: (res) => {
        if (res.confirm) {
          updateManager.applyUpdate()
        }
      }
    })
  })
  
  updateManager.onUpdateFailed(() => {
    uni.showModal({
      title: '更新提示',
      content: '新版本下载失败，请检查网络后重试',
      showCancel: false
    })
  })
  // #endif
  
  // 其他平台暂不支持自动更新
  // #ifndef MP-WEIXIN
  console.log('[Update] 当前平台不支持自动更新检查')
  // #endif
}

// 获取当前版本
export const getCurrentVersion = () => {
  return CURRENT_VERSION
}

// 显示版本信息
export const showVersionInfo = () => {
  uni.showModal({
    title: '版本信息',
    content: `当前版本：v${CURRENT_VERSION}`,
    showCancel: false
  })
}

// 初始化版本检查（App onLaunch 调用）
export const initVersionCheck = () => {
  const savedVersion = uni.getStorageSync(VERSION_KEY)
  
  if (savedVersion !== CURRENT_VERSION) {
    // 版本更新，显示更新日志
    uni.setStorageSync(VERSION_KEY, CURRENT_VERSION)
    
    // 可以在这里显示更新日志
    console.log(`[Update] 版本更新: ${savedVersion} -> ${CURRENT_VERSION}`)
  }
  
  // 检查小程序更新
  checkUpdate()
}

export default {
  checkUpdate,
  getCurrentVersion,
  showVersionInfo,
  initVersionCheck
}
