<template>
  <section class="download">
    <div class="container">
      <div class="download-card">
        <div class="download-bg">
          <div class="bg-circle circle-1"></div>
          <div class="bg-circle circle-2"></div>
          <div class="bg-circle circle-3"></div>
        </div>
        
        <div class="download-content">
          <span class="download-badge">多平台支持</span>
          <h2 class="download-title">随时随地，开始学习</h2>
          <p class="download-subtitle">
            支持 Web 端和移动端，数据实时同步，让你的备考不受设备限制
          </p>
          
          <div class="download-options">
            <div class="download-card-item" @click="$emit('go-web')">
              <div class="card-icon web">
                <el-icon size="32"><Monitor /></el-icon>
              </div>
              <div class="card-info">
                <h3>Web 端</h3>
                <p>浏览器直接访问，无需安装</p>
              </div>
              <el-button type="primary" class="card-btn">
                立即使用
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
            
            <div class="download-card-item" @click="showAppDialog = true">
              <div class="card-icon mobile">
                <el-icon size="32"><Iphone /></el-icon>
              </div>
              <div class="card-info">
                <h3>手机 App</h3>
                <p>iOS / Android 客户端</p>
              </div>
              <el-button class="card-btn-outline">
                <el-icon><Download /></el-icon>
                下载 App
              </el-button>
            </div>
          </div>
          
          <div class="platforms">
            <span class="platform-label">支持平台：</span>
            <div class="platform-tags">
              <span class="platform-tag">微信小程序</span>
              <span class="platform-tag">支付宝小程序</span>
              <span class="platform-tag">百度小程序</span>
              <span class="platform-tag">抖音小程序</span>
              <span class="platform-tag">QQ 小程序</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- App 即将上线弹窗 -->
    <el-dialog 
      v-model="showAppDialog" 
      title="即将上线" 
      width="420px"
      center
      class="app-dialog"
    >
      <div class="dialog-content">
        <div class="dialog-icon">
          <el-icon size="64" color="#6366f1"><Iphone /></el-icon>
        </div>
        <h3>App 正在打包中</h3>
        <p>我们正在将小程序打包为原生 App，敬请期待！</p>
        <div class="dialog-features">
          <div class="dialog-feature">
            <el-icon><Check /></el-icon>
            <span>iOS / Android 双端支持</span>
          </div>
          <div class="dialog-feature">
            <el-icon><Check /></el-icon>
            <span>与 Web 端数据实时同步</span>
          </div>
          <div class="dialog-feature">
            <el-icon><Check /></el-icon>
            <span>离线学习支持</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showAppDialog = false">
          知道了
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { ref } from 'vue'

defineEmits(['go-web'])

const showAppDialog = ref(false)
</script>

<style scoped>
.download {
  padding: 100px 0;
  background: white;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.download-card {
  position: relative;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #4338ca 100%);
  border-radius: 32px;
  padding: 80px 60px;
  overflow: hidden;
}

.download-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(99, 102, 241, 0.2);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -50px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.download-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.download-badge {
  display: inline-block;
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border-radius: 100px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
}

.download-title {
  font-size: 40px;
  font-weight: 800;
  color: white;
  margin-bottom: 16px;
}

.download-subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
  max-width: 600px;
  margin: 0 auto 48px;
}

.download-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  max-width: 700px;
  margin: 0 auto 48px;
}

.download-card-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.download-card-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-4px);
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon.web {
  background: rgba(99, 102, 241, 0.3);
  color: #a5b4fc;
}

.card-icon.mobile {
  background: rgba(16, 185, 129, 0.3);
  color: #6ee7b7;
}

.card-info {
  flex: 1;
  text-align: left;
}

.card-info h3 {
  font-size: 18px;
  font-weight: 700;
  color: white;
  margin-bottom: 4px;
}

.card-info p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.card-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.card-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.card-btn-outline {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: 600;
}

.card-btn-outline:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

.platforms {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.platform-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.platform-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.platform-tag {
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 100px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.dialog-content {
  text-align: center;
  padding: 20px 0;
}

.dialog-icon {
  margin-bottom: 20px;
}

.dialog-content h3 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12px;
}

.dialog-content p {
  font-size: 16px;
  color: #64748b;
  margin-bottom: 24px;
}

.dialog-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  text-align: left;
  max-width: 280px;
  margin: 0 auto;
}

.dialog-feature {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: #475569;
}

.dialog-feature .el-icon {
  color: #10b981;
  font-size: 18px;
}

@media (max-width: 768px) {
  .download {
    padding: 60px 0;
  }
  
  .download-card {
    padding: 48px 24px;
    border-radius: 24px;
  }
  
  .download-title {
    font-size: 28px;
  }
  
  .download-options {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .download-card-item {
    flex-direction: column;
    text-align: center;
  }
  
  .card-info {
    text-align: center;
  }
}
</style>
