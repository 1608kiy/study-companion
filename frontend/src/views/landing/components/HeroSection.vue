<template>
  <section class="hero">
    <div class="hero-bg">
      <div class="hero-gradient"></div>
      <div class="hero-dots"></div>
    </div>
    <div class="container">
      <div class="hero-content" :class="{ 'animate-in': isVisible }">
        <div class="hero-badge">
          <span class="badge-icon">📚</span>
          <span>考公考编必备工具</span>
        </div>
        <h1 class="hero-title">
          记录努力，看见进步
          <br />
          <span class="gradient-text">AI陪伴，心里踏实</span>
        </h1>
        <p class="hero-subtitle">
          智学伴是一个专注于考公考编的学习陪伴工具，通过记录学习时间、写日记、AI分析，
          帮助用户看见自己的努力，心里踏实，更有底气。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="cta-btn" @click="$emit('go-web')">
            <el-icon><Monitor /></el-icon>
            立即使用 Web 端
          </el-button>
          <el-button size="large" class="cta-btn-outline" @click="$emit('download-app')">
            <el-icon><Download /></el-icon>
            下载 App
          </el-button>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-value">143</span>
            <span class="stat-label">测试用例</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">12</span>
            <span class="stat-label">核心功能</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">5</span>
            <span class="stat-label">小程序平台</span>
          </div>
        </div>
      </div>
      <div class="hero-image" :class="{ 'animate-in': isVisible }">
        <div class="mockup-container">
          <div class="mockup-browser">
            <div class="browser-bar">
              <div class="browser-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <div class="browser-url">localhost:3000</div>
            </div>
            <div class="browser-content">
              <div class="mock-sidebar">
                <div class="sidebar-logo">智学伴</div>
                <div class="sidebar-item active">首页</div>
                <div class="sidebar-item">学习</div>
                <div class="sidebar-item">日记</div>
                <div class="sidebar-item">统计</div>
                <div class="sidebar-item">AI助手</div>
              </div>
              <div class="mock-main">
                <div class="mock-header">
                  <div class="mock-title">欢迎回来，学习者 👋</div>
                </div>
                <div class="mock-stats">
                  <div class="mock-stat blue">
                    <div class="mock-stat-value">120</div>
                    <div class="mock-stat-label">今日学习</div>
                  </div>
                  <div class="mock-stat green">
                    <div class="mock-stat-value">7</div>
                    <div class="mock-stat-label">连续打卡</div>
                  </div>
                  <div class="mock-stat orange">
                    <div class="mock-stat-value">3</div>
                    <div class="mock-stat-label">本周日记</div>
                  </div>
                  <div class="mock-stat purple">
                    <div class="mock-stat-value">85%</div>
                    <div class="mock-stat-label">目标完成</div>
                  </div>
                </div>
                <div class="mock-chart">
                  <div class="mock-chart-bars">
                    <div class="bar" style="height: 60%"></div>
                    <div class="bar" style="height: 80%"></div>
                    <div class="bar" style="height: 45%"></div>
                    <div class="bar" style="height: 90%"></div>
                    <div class="bar" style="height: 70%"></div>
                    <div class="bar" style="height: 85%"></div>
                    <div class="bar" style="height: 55%"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'

defineEmits(['go-web', 'download-app'])

const isVisible = ref(false)

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<style scoped>
.hero {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  overflow: hidden;
  padding: 80px 0 40px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  z-index: -1;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.1;
}

.hero-dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle at 1px 1px, rgba(99, 102, 241, 0.15) 1px, transparent 0);
  background-size: 40px 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-content {
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

.hero-content.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 100px;
  font-size: 14px;
  color: #6366f1;
  font-weight: 500;
  margin-bottom: 24px;
}

.badge-icon {
  font-size: 16px;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.2;
  color: #1e293b;
  margin-bottom: 24px;
}

.gradient-text {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 18px;
  line-height: 1.8;
  color: #64748b;
  margin-bottom: 32px;
  max-width: 480px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 48px;
}

.cta-btn {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
  transition: all 0.3s;
}

.cta-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
}

.cta-btn-outline {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: white;
  border: 2px solid #e2e8f0;
  color: #1e293b;
  transition: all 0.3s;
}

.cta-btn-outline:hover {
  border-color: #6366f1;
  color: #6366f1;
  transform: translateY(-2px);
}

.hero-stats {
  display: flex;
  gap: 48px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #1e293b;
}

.stat-label {
  font-size: 14px;
  color: #94a3b8;
  margin-top: 4px;
}

.hero-image {
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  transition-delay: 0.2s;
}

.hero-image.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.mockup-container {
  perspective: 1000px;
}

.mockup-browser {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  transform: rotateY(-5deg) rotateX(5deg);
  transition: transform 0.5s;
}

.mockup-browser:hover {
  transform: rotateY(0deg) rotateX(0deg);
}

.browser-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.browser-dots {
  display: flex;
  gap: 6px;
}

.browser-dots span {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.browser-dots span:nth-child(1) { background: #ef4444; }
.browser-dots span:nth-child(2) { background: #f59e0b; }
.browser-dots span:nth-child(3) { background: #10b981; }

.browser-url {
  flex: 1;
  padding: 6px 12px;
  background: white;
  border-radius: 6px;
  font-size: 12px;
  color: #94a3b8;
  border: 1px solid #e2e8f0;
}

.browser-content {
  display: flex;
  height: 320px;
}

.mock-sidebar {
  width: 120px;
  background: #1e293b;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-logo {
  color: white;
  font-size: 16px;
  font-weight: 700;
  padding: 8px;
  margin-bottom: 16px;
}

.sidebar-item {
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 12px;
  color: #94a3b8;
  transition: all 0.2s;
}

.sidebar-item.active {
  background: rgba(99, 102, 241, 0.2);
  color: #818cf8;
}

.mock-main {
  flex: 1;
  padding: 20px;
  background: #f8fafc;
}

.mock-header {
  margin-bottom: 20px;
}

.mock-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.mock-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.mock-stat {
  padding: 12px;
  background: white;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.mock-stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.mock-stat-label {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.mock-chart {
  background: white;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  padding: 16px;
  height: 120px;
}

.mock-chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 100%;
  gap: 8px;
}

.bar {
  flex: 1;
  background: linear-gradient(180deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 4px 4px 0 0;
  transition: height 0.5s;
}

@media (max-width: 1024px) {
  .container {
    grid-template-columns: 1fr;
    text-align: center;
  }
  
  .hero-subtitle {
    max-width: 100%;
  }
  
  .hero-actions {
    justify-content: center;
  }
  
  .hero-stats {
    justify-content: center;
  }
  
  .hero-image {
    display: none;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .hero-stats {
    gap: 24px;
  }
}
</style>
