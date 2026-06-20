// Markdown 渲染（小程序版）
// 使用 rich-text 组件渲染，无需 DOMPurify

export const renderMarkdown = (text) => {
  if (!text) return ''
  
  let html = text
    // 标题
    .replace(/^### (.*$)/gim, '<h3 style="font-size:28rpx;font-weight:600;margin:16rpx 0 8rpx;">$1</h3>')
    .replace(/^## (.*$)/gim, '<h2 style="font-size:32rpx;font-weight:600;margin:20rpx 0 12rpx;">$1</h2>')
    .replace(/^# (.*$)/gim, '<h1 style="font-size:36rpx;font-weight:700;margin:24rpx 0 16rpx;">$1</h1>')
    // 粗体
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    // 列表项
    .replace(/^- (.*$)/gim, '<div style="padding-left:20rpx;">• $1</div>')
    // 换行
    .replace(/\n/g, '<br/>')
  
  return html
}

export default renderMarkdown
