# 智学伴 UI 设计规范 (Flat Remix)

## 设计风格

Flat Remix — 扁平化 + 微立体、纯色色块、轻量边框、克制的阴影、清晰的信息层级。

## 颜色体系

```css
:root {
  /* 主色 — Indigo */
  --primary: #6366f1;
  --primary-dark: #4f46e5;
  --primary-bg: #eef2ff;

  /* 功能色 */
  --success: #10b981;
  --warning: #f59e0b;
  --danger: #ef4444;

  /* 中性色 — Slate */
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --text-muted: #94a3b8;
  --bg-page: #f8fafc;
  --bg-card: #ffffff;
  --border: #e2e8f0;

  /* 统计卡片四色 */
  --stat-blue: #3b82f6;
  --stat-green: #10b981;
  --stat-orange: #f59e0b;
  --stat-purple: #8b5cf6;
}
```

## 字体

```css
font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
```

| 用途 | 大小 | 粗细 |
|------|------|------|
| 页面标题 | 24px | 700 |
| 卡片标题 | 16px | 600 |
| 统计大数字 | 28px | 700 |
| 正文 | 14px | 400 |
| 辅助文字 | 12-13px | 400 |

## 间距

```css
--page-padding: 24px;   /* 移动端 16px */
--card-gap: 20px;       /* el-row gutter */
--card-padding: 20px;
--section-gap: 24px;
```

## 圆角

```css
--radius: 8px;          /* 按钮、输入框 */
--radius-lg: 12px;      /* 卡片 */
```

## 阴影

Flat Remix 不使用阴影，用边框区分层级：

```css
border: 1px solid var(--border);
box-shadow: none;
```

## 卡片

```css
background: var(--bg-card);
border: 1px solid var(--border);
border-radius: var(--radius-lg);
padding: var(--card-padding);
```

## 按钮

- 主按钮: `background: var(--primary)`, 圆角 8px, 高度 40px, 字重 600
- 次按钮: 透明背景 + 主色边框
- hover: `background: var(--primary-dark)`

## 统计卡片

**模式A — 水平图标+数值（首页、学习页）：**
- 图标区: 44×44px, 圆角 8px, 纯色背景 + 白色图标
- 数值: 28px, bold
- 标签: 13px, `--text-secondary`

**模式B — 全色块居中（统计页）：**
- 整块纯色背景，白色文字

## 图表

- 柱状图: 纯色柱子，宽度 24px，顶部圆角 4px
- 饼图: 环形图，四色体系
- 坐标轴: 浅灰线条，`--text-muted` 标签

## 响应式

| 断点 | 行为 |
|------|------|
| xs (<576px) | 单列，统计卡片 2 列，侧边栏抽屉式 |
| md (≥768px) | 双列布局，统计卡片 4 列 |

## 禁止事项

- 禁止 `linear-gradient`
- 禁止 `box-shadow`（除弹窗等特殊场景）
- 禁止 `backdrop-filter`
- 禁止 `transform: translateY` hover 效果
- 禁止装饰性 CSS 动画
- 禁止 `!important`
