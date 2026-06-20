# 智学伴 - 微信小程序

考公考编学习陪伴平台，与 Web 端共用同一后端，数据完全互通。

## 功能特性

- 🏠 **首页**：学习统计、打卡、7天趋势
- ⏱️ **学习**：番茄钟计时、科目选择、记录管理
- 📝 **日记**：写日记、AI生成、日期导航
- 📊 **统计**：月度统计、科目分布、月份切换
- 🤖 **AI助手**：智能对话、周报/月报、专注度评估
- ⚙️ **设置**：个人信息、学习目标、科目管理

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Uniapp | 3.x | 跨平台框架 |
| Vue | 3.x + Composition API | 前端框架 |
| Pinia | 2.x | 状态管理 |

## 目录结构

```
miniapp/
├── api/                    # API 层
│   ├── index.js            # 请求封装（拦截器）
│   └── modules.js          # 端点函数
├── components/             # 公共组件
│   ├── main-layout.vue     # 主布局
│   └── tab-bar.vue         # 自定义TabBar
├── config/                 # 配置
│   └── index.js            # 环境配置
├── pages/                  # 页面
│   ├── home/home.vue       # 首页
│   ├── study/study.vue     # 学习
│   ├── diary/diary.vue     # 日记
│   ├── stats/stats.vue     # 统计
│   ├── ai/ai.vue           # AI助手
│   ├── settings/settings.vue # 设置
│   ├── login/login.vue     # 登录
│   ├── register/register.vue # 注册
│   └── subjects/subjects.vue # 科目管理
├── store/                  # 状态管理
│   ├── user.js             # 用户状态
│   └── study.js            # 学习状态
├── utils/                  # 工具函数
│   ├── storage.js          # 本地存储
│   └── markdown.js         # Markdown渲染
├── App.vue                 # 应用入口
├── main.js                 # 初始化
├── pages.json              # 路由配置
├── manifest.json           # 应用配置
└── uni.scss                # 全局样式
```

## 快速开始

### 环境要求

- Node.js 18+
- HBuilderX（推荐）或 微信开发者工具

### 1. 安装依赖

```bash
cd miniapp
npm install
```

### 2. 配置后端地址

编辑 `config/index.js`，修改生产环境地址：

```javascript
production: {
  baseUrl: 'https://your-domain.com/api/v1'
}
```

### 3. 配置小程序 AppID

编辑 `manifest.json`，填入各平台的 AppID：

```json
{
  "mp-weixin": {
    "appid": "your-wechat-appid"
  },
  "mp-alipay": {
    "appid": "your-alipay-appid"
  }
}
```

### 4. 运行

```bash
# 微信小程序
npm run dev:mp-weixin

# 支付宝小程序
npm run dev:mp-alipay

# 百度小程序
npm run dev:mp-baidu

# 抖音小程序
npm run dev:mp-toutiao

# H5 预览
npm run dev:h5
```

### 5. 构建

```bash
npm run build:mp-weixin
```

## 平台支持

| 平台 | 状态 |
|------|------|
| 微信小程序 | ✅ 支持 |
| 支付宝小程序 | ✅ 支持 |
| 百度小程序 | ✅ 支持 |
| 抖音小程序 | ✅ 支持 |
| QQ小程序 | ✅ 支持 |
| H5 | ✅ 支持 |

## 与 Web 端的关系

- 共用同一后端 API
- 同一账号数据完全同步
- 计时器状态通过 Redis 跨端同步
- 用户设置实时同步

## 开发规范

- 使用 Vue3 Composition API（`<script setup>`）
- 使用 Pinia 管理状态
- 使用 `uni.request` 封装 API
- 使用 `uni.setStorageSync` 存储本地数据
- 页面使用 `main-layout` 组件包裹
- 错误处理统一使用 `uni.showToast`

## 注意事项

1. **网络请求**：小程序必须使用 HTTPS，且域名需在后台配置白名单
2. **本地存储**：使用 `uni.setStorageSync` 而非 `localStorage`
3. **路由**：使用 `pages.json` 配置，不支持 Vue Router
4. **包体积**：微信主包限制 2MB，注意资源大小
5. **样式**：使用 rpx 单位，自动适配不同屏幕

## 许可证

MIT License
