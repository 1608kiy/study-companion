// 分享功能封装

// 默认分享配置
const defaultShareConfig = {
  title: '智学伴 - 考公考编学习陪伴平台',
  path: '/pages/home/home',
  imageUrl: '' // 可以设置分享图片
}

// 生成学习打卡分享内容
export const generateCheckInShare = (stats) => {
  const { duration = 0, streak = 0 } = stats || {}
  return {
    title: `我在智学伴学习了${duration}分钟，连续打卡${streak}天！`,
    path: '/pages/home/home'
  }
}

// 生成学习报告分享内容
export const generateReportShare = (type, stats) => {
  const { totalDays = 0, totalDuration = 0 } = stats || {}
  const typeText = type === 'weekly' ? '周' : '月'
  const hours = Math.floor(totalDuration / 60)
  
  return {
    title: `我的${typeText}报：学习${totalDays}天，累计${hours}小时`,
    path: '/pages/stats/stats'
  }
}

// 生成日记分享内容
export const generateDiaryShare = (diary) => {
  const date = diary?.diaryDate || new Date().toISOString().split('T')[0]
  return {
    title: `${date} 学习日记`,
    path: '/pages/diary/diary'
  }
}

// 页面分享配置（用于 mixin 或组合式函数）
export const useShareConfig = (config = {}) => {
  const shareConfig = { ...defaultShareConfig, ...config }
  
  // 微信小程序分享
  // #ifdef MP-WEIXIN
  const onShareAppMessage = () => shareConfig
  const onShareTimeline = () => ({
    title: shareConfig.title,
    query: '',
    imageUrl: shareConfig.imageUrl
  })
  // #endif
  
  // 其他平台
  // #ifndef MP-WEIXIN
  const onShareAppMessage = () => shareConfig
  const onShareTimeline = () => shareConfig
  // #endif
  
  return {
    onShareAppMessage,
    onShareTimeline
  }
}

// 主动触发分享（显示分享菜单）
export const showShareMenu = () => {
  // #ifdef MP-WEIXIN
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
  // #endif
  
  // #ifndef MP-WEIXIN
  uni.showShareMenu({
    withShareTicket: true
  })
  // #endif
}

export default {
  generateCheckInShare,
  generateReportShare,
  generateDiaryShare,
  useShareConfig,
  showShareMenu
}
