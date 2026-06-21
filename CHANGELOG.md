# Changelog

## v1.1.0 (2026-06-21)

### 新增功能

#### 学习效率分析
- 新增 `GET /study-records/efficiency` 接口
- 每小时学习时段分布分析
- 科目时长分布统计
- 专注度趋势图表（最近30天）
- 最佳学习时段/学习日推荐
- 前端 `EfficiencyView.vue` 可视化页面

#### 考试倒计时
- 新增 `Exam` 实体和 CRUD API
- 前端 `ExamCountdown.vue` 组件
- 首页显示考试倒计时
- 紧急考试（≤7天）高亮

#### 学习资料管理
- 新增 `LearningMaterial` 实体和 CRUD API
- 支持 PDF/Word/Excel/PPT/图片等格式上传
- 前端 `MaterialsView.vue` 页面
- 资料搜索、收藏、删除功能

#### 密码重置
- 新增 `POST /auth/forgot-password` 接口
- 新增 `POST /auth/reset-password` 接口
- 前端 `ForgotPasswordView.vue` 页面
- 小程序 `forgot-password` 页面

#### 补卡 UI
- 新增 `ReplenishDialog.vue` 组件
- 首页打卡区域补卡申请入口
- 小程序补卡弹窗
- AI 判断结果展示

#### 小程序适配
- 效率分析页面
- 学习资料页面
- 忘记密码页面
- 首页快捷入口

### 性能优化

- 连续打卡计算从 N+1 查询优化为单次查询
- 学习资料批量加载科目（避免 N+1）
- 分页参数验证（最大每页 100 条）

### 安全修复

- 使用 `SecureRandom` 替代 `Random` 生成验证码
- 配置静态资源服务（上传文件可正常访问）
- 分页参数边界验证

### Bug 修复

- 修复 SPA 导航问题（`router.push` 替代 `window.location.href`）
- 修复 `ai_chat_history` 表缺失问题
- 修复 `study_record` 表缺少 `ai_allow_modify`/`ai_modify_reason` 列

### 文档更新

- AGENTS.md 更新 API 端点列表
- 更新小程序页面数（12 页）
- 新增 CHANGELOG.md

---

## v1.0.0 (2026-06-21)

### 初始版本

- 用户注册/登录（JWT 认证）
- 科目管理（预设 + 自定义）
- 学习计时器（开始/暂停/恢复/停止）
- 学习记录 CRUD
- 学习统计（每日/每周/每月）
- AI 日记生成
- 打卡系统
- 目标管理
- AI 对话（MiMo 模型）
- 数据真实性保障（AI 审批机制）
- 跨端数据同步（Web + 小程序）
