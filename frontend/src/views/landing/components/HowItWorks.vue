<template>
  <section class="how-it-works">
    <div class="container">
      <div class="section-header">
        <span class="section-badge">使用流程</span>
        <h2 class="section-title">三步开始你的备考之旅</h2>
        <p class="section-subtitle">
          简单注册，立即开始学习，AI 帮你看见进步
        </p>
      </div>
      
      <div class="steps-container">
        <div class="steps-line"></div>
        <div 
          v-for="(step, index) in steps" 
          :key="index"
          class="step-card"
          :class="{ 'animate-in': visibleSteps[index] }"
          :style="{ transitionDelay: `${index * 200}ms` }"
        >
          <div class="step-number">{{ index + 1 }}</div>
          <div class="step-icon">{{ step.icon }}</div>
          <h3 class="step-title">{{ step.title }}</h3>
          <p class="step-desc">{{ step.description }}</p>
        </div>
      </div>
      
      <div class="cta-container">
        <p class="cta-text">准备好了吗？</p>
        <el-button type="primary" size="large" class="cta-btn" @click="$emit('go-web')">
          立即开始
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'

defineEmits(['go-web'])

const steps = [
  {
    icon: '📝',
    title: '注册账号',
    description: '30秒快速注册，创建你的学习空间，开始记录每一天的努力'
  },
  {
    icon: '⏱️',
    title: '开始学习',
    description: '使用番茄钟计时，选择科目，专注学习，系统自动记录时长'
  },
  {
    icon: '📊',
    title: '看见进步',
    description: 'AI 分析学习数据，生成周报月报，让你的努力看得见、心里踏实'
  }
]

const visibleSteps = ref(steps.map(() => false))

onMounted(() => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const index = parseInt(entry.target.dataset.index)
        visibleSteps.value[index] = true
      }
    })
  }, { threshold: 0.2 })
  
  document.querySelectorAll('.step-card').forEach((card, index) => {
    card.dataset.index = index
    observer.observe(card)
  })
})
</script>

<style scoped>
.how-it-works {
  padding: 100px 0;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-header {
  text-align: center;
  margin-bottom: 64px;
}

.section-badge {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
  border-radius: 100px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
}

.section-title {
  font-size: 40px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 16px;
}

.section-subtitle {
  font-size: 18px;
  color: #64748b;
  max-width: 600px;
  margin: 0 auto;
}

.steps-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 48px;
  position: relative;
  margin-bottom: 64px;
}

.steps-line {
  position: absolute;
  top: 48px;
  left: 20%;
  right: 20%;
  height: 2px;
  background: linear-gradient(90deg, #6366f1 0%, #8b5cf6 50%, #a855f7 100%);
  z-index: 0;
}

.step-card {
  text-align: center;
  position: relative;
  z-index: 1;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.step-card.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.step-number {
  position: absolute;
  top: -16px;
  left: 50%;
  transform: translateX(-50%);
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

.step-icon {
  width: 96px;
  height: 96px;
  margin: 0 auto 24px;
  background: white;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border: 2px solid #e2e8f0;
}

.step-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12px;
}

.step-desc {
  font-size: 15px;
  color: #64748b;
  line-height: 1.6;
  max-width: 280px;
  margin: 0 auto;
}

.cta-container {
  text-align: center;
}

.cta-text {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 24px;
}

.cta-btn {
  height: 56px;
  padding: 0 40px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 16px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
  transition: all 0.3s;
}

.cta-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
}

@media (max-width: 1024px) {
  .steps-line {
    display: none;
  }
  
  .steps-container {
    grid-template-columns: 1fr;
    gap: 32px;
  }
}

@media (max-width: 768px) {
  .how-it-works {
    padding: 60px 0;
  }
  
  .section-title {
    font-size: 28px;
  }
  
  .step-icon {
    width: 80px;
    height: 80px;
    font-size: 32px;
  }
}
</style>
