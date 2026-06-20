// API 配置
// 开发环境使用代理，生产环境使用真实地址
const config = {
  // 开发环境（H5 代理）
  development: {
    baseUrl: '/api/v1'
  },
  // 生产环境（小程序需要完整 HTTPS 地址）
  production: {
    baseUrl: 'https://your-domain.com/api/v1'
  }
}

// 获取当前环境
const getEnv = () => {
  // #ifdef H5
  return 'development'
  // #endif
  // #ifdef MP-WEIXIN || MP-ALIPAY || MP-BAIDU || MP-TOUTIAO || MP-QQ
  return 'production'
  // #endif
}

export const getConfig = () => {
  const env = getEnv()
  return config[env] || config.development
}

export default config
