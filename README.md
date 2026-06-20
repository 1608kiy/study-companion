# 智学伴 - AI学习陪伴平台

> 记录努力，看见进步，AI陪伴，心里踏实

## 项目简介

智学伴是一个专注于考公考编的学习陪伴工具，通过记录学习时间、写日记、AI分析，帮助用户看见自己的努力，心里踏实，更有底气。

## 核心功能

- **番茄钟计时**：精确记录每次学习的时长
- **学习打卡**：每日打卡，记录连续学习天数
- **强制日记**：写完日记才能打卡成功，促进深度反思
- **AI智能分析**：根据学习数据给出个性化建议、周报/月报
- **数据可视化**：日历热力图、统计图表，让努力看得见
- **深色模式**：支持深色/浅色主题切换
- **移动端适配**：底部导航栏、响应式布局

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + Java 17 |
| 前端 | Vue3 + Vite + Element Plus + Pinia |
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
├── miniapp/               # 小程序项目（未开始）
├── AGENTS.md              # AI开发指南
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

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `AI_API_KEY` | MiMo API密钥 | `your-api-key`（占位符） |
| `SPRING_DATASOURCE_URL` | 数据库连接 | `jdbc:mysql://localhost:3306/study_companion` |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | `root` |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | `root` |
| `SPRING_DATA_REDIS_HOST` | Redis地址 | `localhost` |

## API 端点

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/auth/register` | 注册 |
| POST | `/api/v1/auth/login` | 登录 |

### 用户

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/user/profile` | 获取个人信息 |
| PUT | `/api/v1/user/profile` | 更新个人信息 |
| DELETE | `/api/v1/user/delete` | 注销账号 |

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
| PUT | `/api/v1/diaries/{id}` | 更新日记 |
| DELETE | `/api/v1/diaries/{id}` | 删除日记 |
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

### AI

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/ai/chat` | AI对话（支持历史记录） |
| POST | `/api/v1/ai/weekly-report` | 生成周报 |
| POST | `/api/v1/ai/monthly-report` | 生成月报 |
| POST | `/api/v1/ai/focus-judge` | 专注度评估 |
| GET | `/api/v1/ai/share` | 生成分享图 |

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

## 测试

- 后端：78个单元测试（Mockito + JUnit5）
- 前端：49个单元测试（Vitest）

```bash
# 运行所有测试
mvn test -f backend/pom.xml && cd frontend && npm test
```

## 许可证

MIT License
