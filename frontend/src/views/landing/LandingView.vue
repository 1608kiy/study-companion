<template>
  <div class="landing">
    <!-- 导航栏 -->
    <nav class="navbar" :class="{ scrolled: isScrolled }">
      <div class="nav-container">
        <div class="nav-logo" @click="scrollToTop">
          <span class="logo-icon">📚</span>
          <span class="logo-text">智学伴</span>
        </div>
        
        <div class="nav-links" :class="{ open: mobileMenuOpen }">
          <a href="#features" @click.prevent="scrollTo('features'); mobileMenuOpen = false">功能</a>
          <a href="#how-it-works" @click.prevent="scrollTo('how-it-works'); mobileMenuOpen = false">使用流程</a>
          <a href="#download" @click.prevent="scrollTo('download'); mobileMenuOpen = false">下载</a>
          <a href="/login" @click.prevent="$router.push('/login')">登录</a>
          <el-button type="primary" size="small" class="nav-cta" @click="$router.push('/register')">
            免费注册
          </el-button>
        </div>
        
        <el-icon 
          class="mobile-menu-btn" 
          @click="mobileMenuOpen = !mobileMenuOpen"
        >
          <Close v-if="mobileMenuOpen" />
          <Menu v-else />
        </el-icon>
      </div>
    </nav>
    
    <!-- 主要内容 -->
    <main>
      <HeroSection 
        id="hero"
        @go-web="goToWeb" 
        @download-app="showAppDialog = true" 
      />
      
      <FeaturesSection id="features" />
      
      <HowItWorks id="how-it-works" @go-web="goToWeb" />
      
      <DownloadSection 
        id="download" 
        @go-web="goToWeb" 
      />
    </main>
    
    <FooterSection />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import HeroSection from './components/HeroSection.vue'
import FeaturesSection from './components/FeaturesSection.vue'
import HowItWorks from './components/HowItWorks.vue'
import DownloadSection from './components/DownloadSection.vue'
import FooterSection from './components/FooterSection.vue'

const router = useRouter()

const isScrolled = ref(false)
const mobileMenuOpen = ref(false)

const goToWeb = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/home')
  } else {
    router.push('/login')
  }
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const scrollTo = (id) => {
  const element = document.getElementById(id)
  if (element) {
    const offset = 80
    const top = element.getBoundingClientRect().top + window.pageYOffset - offset
    window.scrollTo({ top, behavior: 'smooth' })
  }
}

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.landing {
  min-height: 100vh;
  background: white;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 16px 0;
  transition: all 0.3s;
  background: transparent;
}

.navbar.scrolled {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e2e8f0;
  padding: 12px 0;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-links a {
  font-size: 15px;
  color: #475569;
  text-decoration: none;
  transition: color 0.2s;
  font-weight: 500;
}

.nav-links a:hover {
  color: #6366f1;
}

.nav-cta {
  height: 36px;
  padding: 0 20px;
  font-weight: 600;
  border-radius: 10px;
}

.mobile-menu-btn {
  display: none;
  font-size: 24px;
  color: #1e293b;
  cursor: pointer;
}

@media (max-width: 768px) {
  .mobile-menu-btn {
    display: block;
  }
  
  .nav-links {
    position: fixed;
    top: 0;
    right: -100%;
    width: 280px;
    height: 100vh;
    background: white;
    flex-direction: column;
    align-items: flex-start;
    padding: 80px 24px 24px;
    gap: 20px;
    box-shadow: -10px 0 30px rgba(0, 0, 0, 0.1);
    transition: right 0.3s;
  }
  
  .nav-links.open {
    right: 0;
  }
  
  .nav-links a {
    font-size: 18px;
    padding: 8px 0;
  }
  
  .nav-cta {
    width: 100%;
    margin-top: 16px;
  }
}
</style>
