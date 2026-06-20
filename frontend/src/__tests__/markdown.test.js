import { describe, it, expect, vi } from 'vitest'

// Mock DOMPurify for test environment
vi.mock('dompurify', () => ({
  default: {
    sanitize: (html) => html
  }
}))

import { renderMarkdown } from '../utils/markdown'

describe('renderMarkdown', () => {
  it('空字符串返回空', () => {
    expect(renderMarkdown('')).toBe('')
    expect(renderMarkdown(null)).toBe('')
    expect(renderMarkdown(undefined)).toBe('')
  })

  it('转换标题 h1', () => {
    const result = renderMarkdown('# 标题')
    expect(result).toContain('<h1>')
    expect(result).toContain('标题')
  })

  it('转换标题 h2', () => {
    const result = renderMarkdown('## 标题')
    expect(result).toContain('<h2>')
    expect(result).toContain('标题')
  })

  it('转换标题 h3', () => {
    const result = renderMarkdown('### 标题')
    expect(result).toContain('<h3>')
    expect(result).toContain('标题')
  })

  it('转换粗体', () => {
    const result = renderMarkdown('**粗体文字**')
    expect(result).toContain('<strong>')
    expect(result).toContain('粗体文字')
  })

  it('转换斜体', () => {
    const result = renderMarkdown('*斜体文字*')
    expect(result).toContain('<em>')
    expect(result).toContain('斜体文字')
  })

  it('转换换行', () => {
    const result = renderMarkdown('第一行\n第二行')
    expect(result).toContain('<br>')
  })

  it('混合格式', () => {
    const text = '# 标题\n\n**粗体** 和 *斜体*'
    const result = renderMarkdown(text)
    expect(result).toContain('<h1>')
    expect(result).toContain('<strong>')
    expect(result).toContain('<em>')
    expect(result).toContain('<br>')
  })

  it('保留纯文本', () => {
    const result = renderMarkdown('普通文本')
    expect(result).toBe('普通文本')
  })
})
