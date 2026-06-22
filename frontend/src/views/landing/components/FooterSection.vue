<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-brand">
          <div class="footer-logo">
            <span class="logo-icon">📚</span>
            <span class="logo-text">智学伴</span>
          </div>
          <p class="footer-desc">
            记录努力，看见进步，AI陪伴，心里踏实
          </p>
          <div class="footer-social">
            <a href="https://github.com/1608kiy/study-companion" target="_blank" class="social-link">
              <el-icon size="20"><Link /></el-icon>
            </a>
          </div>
        </div>
        
        <div class="footer-links">
          <div class="link-group">
            <h4>产品</h4>
            <ul>
              <li><a href="#features" @click.prevent="scrollTo('features')">功能介绍</a></li>
              <li><a href="#download" @click.prevent="scrollTo('download')">下载</a></li>
              <li><a href="/login" @click.prevent="$router.push('/login')">登录</a></li>
              <li><a href="/register" @click.prevent="$router.push('/register')">注册</a></li>
            </ul>
          </div>
          
          <div class="link-group">
            <h4>资源</h4>
            <ul>
              <li><a href="https://github.com/1608kiy/study-companion" target="_blank">GitHub</a></li>
              <li><a href="https://github.com/1608kiy/study-companion/blob/main/README.md" target="_blank">文档</a></li>
              <li><a href="https://github.com/1608kiy/study-companion/blob/main/CHANGELOG.md" target="_blank">更新日志</a></li>
            </ul>
          </div>
          
          <div class="link-group">
            <h4>联系方式</h4>
            <ul>
              <li><a href="https://github.com/1608kiy/study-companion/issues" target="_blank">反馈建议</a></li>
              <li><a href="https://github.com/1608kiy/study-companion/issues" target="_blank">问题报告</a></li>
            </ul>
          </div>
        </div>
      </div>
      
      <div class="footer-bottom">
        <p class="copyright">
          &copy; {{ currentYear }} 智学伴. All rights reserved.
        </p>
        <p class="tech-stack">
          Built with Vue3 + Spring Boot + MySQL
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentYear = computed(() => new Date().getFullYear())

const scrollTo = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<style scoped>
.footer {
  padding: 80px 0 40px;
  background: #0f172a;
  color: #94a3b8;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 60px;
  margin-bottom: 60px;
}

.footer-brand {
  max-width: 300px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: white;
}

.footer-desc {
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 24px;
}

.footer-social {
  display: flex;
  gap: 12px;
}

.social-link {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  text-decoration: none;
  transition: all 0.2s;
}

.social-link:hover {
  background: rgba(99, 102, 241, 0.2);
  color: #818cf8;
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
}

.link-group h4 {
  font-size: 16px;
  font-weight: 600;
  color: white;
  margin-bottom: 20px;
}

.link-group ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.link-group a {
  font-size: 14px;
  color: #94a3b8;
  text-decoration: none;
  transition: color 0.2s;
}

.link-group a:hover {
  color: #818cf8;
}

.footer-bottom {
  padding-top: 32px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.copyright {
  font-size: 14px;
  color: #64748b;
}

.tech-stack {
  font-size: 13px;
  color: #475569;
}

@media (max-width: 1024px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  
  .footer-brand {
    max-width: 100%;
  }
}

@media (max-width: 768px) {
  .footer {
    padding: 60px 0 32px;
  }
  
  .footer-links {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }
  
  .footer-bottom {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
}
</style>
