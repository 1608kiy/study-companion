# 智学伴 - AI学习陪伴平台

> 记录努力，看见进步，AI陪伴，心里踏实

## 项目简介

智学伴是一个专注于考公考编的学习陪伴工具，通过记录学习时间、写日记、AI分析，帮助用户看见自己的努力，心里踏实，更有底气。

## 核心功能

### 学习管理
- **番茄钟计时**：精确记录每次学习的时长，支持暂停/恢复
- **学习打卡**：每日打卡，记录连续学习天数
- **强制日记**：写完日记才能打卡成功，促进深度反思
- **学习效率分析**：最佳学习时段、科目分布、专注度趋势

### AI 功能
- **AI智能对话**：考公问题解答，支持上下文记忆
- **AI日记生成**：根据学习数据自动生成日记
- **AI周报/月报**：定期学习报告，发现问题与进步
- **AI专注度评估**：分析学习专注度，提供改进建议

### 考试管理
- **考试倒计时**：设置考试日期，首页显示倒计时
- **目标管理**：每日/每周/每月学习目标
- **补卡申请**：断签后可申请补签，AI判断是否允许

### 学习资料
- **资料上传**：支持 PDF、Word、Excel、PPT、图片等格式
- **资料管理**：搜索、分类、收藏功能
- **跨端同步**：Web 和小程序数据实时同步

### 跨平台
- **Web 端**：Vue3 + Element Plus，响应式设计
- **小程序**：Uniapp 3.x，支持微信/支付宝/百度/抖音/QQ

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + Java 17 |
| 前端 | Vue3 + Vite + Element Plus + Pinia |
| 小程序 | Uniapp 3.x (Vue3) |
| 数据库 | MySQL 8.0 + Redis 7 |
| AI | MiMo (OpenAI兼容接口) |

## 项目结构

```
study-companion/
├── backend/               # 后端项目
│   ├── src/main/java/     # Java源码
│   ├── src/test/java/     # 测试代码
│   └── sql/               # 数据库脚本
├── frontend/              # 前端项目
│   ├── src/               # Vue源码
│   └── src/__tests__/     # 测试代码
├── miniapp/               # 小程序项目
│   ├── pages/             # 页面
│   ├── api/               # API层
│   └── store/             # 状态管理
├── AGENTS.md              # AI开发指南
├── CHANGELOG.md           # 更新日志
└── README.md              # 本文件
```

## 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- MySQL 8.0
- Redis 7

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE study_companion CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 初始化表结构
mysql -u root -p study_companion < backend/sql/init.sql
```

### 2. 配置环境变量

```bash
# AI API Key（必填，否则AI功能不可用）
export AI_API_KEY="your-mimo-api-key"

# JWT密钥（可选，有默认值）
export JWT_SECRET="your-jwt-secret"

# 数据库配置（可选，有默认值）
export DB_USERNAME="root"
export DB_PASSWORD="root"
```

### 3. 后端启动

```bash
cd backend
mvn spring-boot:run
# 或指定内存限制
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx256m"
```

后端启动在 http://localhost:8080

### 4. 前端启动

```bash
cd frontend
npm install --legacy-peer-deps  # 必须使用 --legacy-peer-deps
npm run dev
```

前端启动在 http://localhost:3000

### 5. 小程序启动

```bash
cd miniapp
npm install
npm run dev:mp-weixin  # 微信小程序
npm run dev:h5         # H5预览
```

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `AI_API_KEY` | MiMo API密钥 | `your-api-key`（占位符） |
| `JWT_SECRET` | JWT签名密钥 | `study-companion-default-secret` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `root` |
| `REDIS_PASSWORD` | Redis密码 | 空 |

## API 端点

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/auth/register` | 注册 |
| POST | `/api/v1/auth/login` | 登录 |
| POST | `/api/v1/auth/forgot-password` | 忘记密码 |
| POST | `/api/v1/auth/reset-password` | 重置密码 |

### 用户

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/user/profile` | 获取个人信息 |
| PUT | `/api/v1/user/profile` | 更新个人信息 |
| DELETE | `/api/v1/user/delete` | 注销账号 |
| POST | `/api/v1/user/logout` | 登出 |

### 科目

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/subjects` | 获取科目列表 |
| GET | `/api/v1/subjects/preset` | 获取预设科目 |
| POST | `/api/v1/subjects` | 创建科目 |
| PUT | `/api/v1/subjects/{id}` | 更新科目 |
| DELETE | `/api/v1/subjects/{id}` | 删除科目 |

### 学习记录

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/study-records/timer/start` | 开始计时 |
| POST | `/api/v1/study-records/timer/pause` | 暂停计时 |
| POST | `/api/v1/study-records/timer/resume` | 恢复计时 |
| POST | `/api/v1/study-records/timer/stop` | 停止计时 |
| GET | `/api/v1/study-records/timer/state` | 获取计时状态 |
| GET | `/api/v1/study-records` | 获取记录列表 |
| GET | `/api/v1/study-records/paged` | 获取记录列表（分页） |
| GET | `/api/v1/study-records/stats` | 获取学习统计 |
| GET | `/api/v1/study-records/efficiency` | 获取效率分析 |
| PUT | `/api/v1/study-records/{id}` | 更新记录 |
| DELETE | `/api/v1/study-records/{id}` | 删除记录 |

### 打卡

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/check-in/today` | 今日打卡状态 |
| POST | `/api/v1/check-in` | 打卡 |
| GET | `/api/v1/check-in/history` | 打卡历史 |
| POST | `/api/v1/check-in/miss` | 记录缺卡 |
| POST | `/api/v1/check-in/miss/{id}/ai-judge` | AI判断补签 |
| POST | `/api/v1/check-in/miss/{id}/replenish` | 补签 |

### 日记

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/diaries` | 获取日记列表 |
| GET | `/api/v1/diaries/paged` | 获取日记列表（分页） |
| GET | `/api/v1/diaries/date/{date}` | 获取指定日期日记 |
| POST | `/api/v1/diaries` | 创建日记 |
| POST | `/api/v1/diaries/generate` | AI生成日记 |
| POST | `/api/v1/diaries/{id}/regenerate` | 重新生成日记 |

### 目标

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/goals` | 获取目标列表 |
| POST | `/api/v1/goals` | 创建目标 |
| PUT | `/api/v1/goals/{id}` | 更新目标 |
| DELETE | `/api/v1/goals/{id}` | 删除目标 |
| POST | `/api/v1/goals/ai-suggest` | AI建议目标 |
| GET | `/api/v1/goals/stats/daily` | 每日统计 |
| GET | `/api/v1/goals/stats/weekly` | 每周统计 |
| GET | `/api/v1/goals/stats/monthly` | 每月统计 |
| GET | `/api/v1/goals/stats/calendar` | 月历统计 |

### 考试

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/exams` | 获取考试列表 |
| POST | `/api/v1/exams` | 创建考试 |
| PUT | `/api/v1/exams/{id}` | 更新考试 |
| DELETE | `/api/v1/exams/{id}` | 删除考试 |

### 学习资料

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/materials` | 获取资料列表 |
| GET | `/api/v1/materials/{id}` | 获取资料详情 |
| POST | `/api/v1/materials` | 上传资料 |
| PUT | `/api/v1/materials/{id}` | 更新资料 |
| DELETE | `/api/v1/materials/{id}` | 删除资料 |
| POST | `/api/v1/materials/{id}/favorite` | 切换收藏 |

### AI

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/ai/chat` | AI对话（支持历史记录） |
| POST | `/api/v1/ai/weekly-report` | 生成周报 |
| POST | `/api/v1/ai/monthly-report` | 生成月报 |
| POST | `/api/v1/ai/focus-judge` | 专注度评估 |
| GET | `/api/v1/ai/share` | 生成分享图 |
| GET | `/api/v1/ai/chat/history` | 获取聊天历史 |
| DELETE | `/api/v1/ai/chat/history` | 清空聊天历史 |

## 开发命令

### 后端

```bash
mvn compile -f backend/pom.xml          # 编译检查
mvn test -f backend/pom.xml             # 运行测试
mvn spring-boot:run -f backend/pom.xml  # 启动服务
```

### 前端

```bash
cd frontend
npm install --legacy-peer-deps   # 安装依赖
npm run dev                      # 开发服务器
npm test                         # 运行测试
npm run build                    # 生产构建
```

### 小程序

```bash
cd miniapp
npm install                      # 安装依赖
npm run dev:mp-weixin            # 微信小程序开发
npm run dev:mp-alipay            # 支付宝小程序开发
npm run dev:h5                   # H5预览
npm run build:mp-weixin          # 微信小程序构建
```

## 测试

- 后端：85个单元测试（Mockito + JUnit5）
- 前端：58个单元测试（Vitest）

```bash
# 运行所有测试
mvn test -f backend/pom.xml && cd frontend && npm test
```

## 数据真实性

**核心原则**：学习数据一旦创建，不可修改。所有修改需要AI审批。

- **日记**：不可编辑删除，只能AI重新生成
- **学习记录**：不可删除，修改需要AI审批（Redis Token，10分钟有效）
- **补卡**：需要AI判断是否允许

## 跨端同步

- Web 和小程序共享同一后端API
- 数据存储在MySQL，实时同步
- AI对话历史跨端共享
- 计时器状态通过Redis同步

## 更新日志

详见 [CHANGELOG.md](CHANGELOG.md)

## 许可证

MIT License
