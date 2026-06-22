<template>
  <section class="features">
    <div class="container">
      <div class="section-header">
        <span class="section-badge">核心功能</span>
        <h2 class="section-title">为考公人量身打造</h2>
        <p class="section-subtitle">
          每一个功能都围绕考公备考场景设计，让学习更高效、更有底气
        </p>
      </div>
      
      <div class="features-grid">
        <div 
          v-for="(feature, index) in features" 
          :key="index"
          class="feature-card"
          :class="{ 'animate-in': visibleCards[index] }"
          :style="{ transitionDelay: `${index * 100}ms` }"
        >
          <div class="feature-icon" :style="{ background: feature.bg }">
            <span>{{ feature.icon }}</span>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.description }}</p>
          <ul class="feature-list">
            <li v-for="item in feature.items" :key="item">
              <el-icon><Check /></el-icon>
              {{ item }}
            </li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const features = [
  {
    icon: '⏱️',
    title: '番茄钟计时',
    description: '精确记录每次学习的时长，让努力可视化',
    bg: 'linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%)',
    items: ['开始/暂停/恢复/停止', '科目标签分类', '学习时长统计', '每日/每周/每月报表']
  },
  {
    icon: '📝',
    title: 'AI 日记',
    description: '写完日记才能打卡，促进深度反思',
    bg: 'linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)',
    items: ['强制日记机制', 'AI 自动生成', 'Markdown 编辑', '图片附件支持']
  },
  {
    icon: '🤖',
    title: 'AI 智能助手',
    description: '你的专属考公顾问，随时解答疑问',
    bg: 'linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%)',
    items: ['考公问题解答', '周报/月报生成', '专注度评估', '学习建议']
  },
  {
    icon: '📊',
    title: '学习效率分析',
    description: '发现最佳学习时段，优化备考策略',
    bg: 'linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)',
    items: ['时段分布分析', '科目专注度对比', '学习趋势图表', '个性化建议']
  },
  {
    icon: '📅',
    title: '考试倒计时',
    description: '目标明确，倒计时提醒，备考更有紧迫感',
    bg: 'linear-gradient(135deg, #fee2e2 0%, #fecaca 100%)',
    items: ['国考/省考/事业编', '倒计时天数', '目标考试标记', '考试地点记录']
  },
  {
    icon: '📁',
    title: '学习资料管理',
    description: '上传笔记文档，随时随地复习',
    bg: 'linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%)',
    items: ['PDF/Word/Excel', '科目标签分类', '收藏功能', '搜索功能']
  }
]

const visibleCards = ref(features.map(() => false))

onMounted(() => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const index = parseInt(entry.target.dataset.index)
        visibleCards.value[index] = true
      }
    })
  }, { threshold: 0.1 })
  
  document.querySelectorAll('.feature-card').forEach((card, index) => {
    card.dataset.index = index
    observer.observe(card)
  })
})
</script>

<style scoped>
.features {
  padding: 100px 0;
  background: white;
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

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.feature-card {
  padding: 32px;
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  opacity: 0;
  transform: translateY(30px);
}

.feature-card.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border-color: transparent;
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 20px;
}

.feature-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12px;
}

.feature-desc {
  font-size: 15px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 20px;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #475569;
}

.feature-list li .el-icon {
  color: #10b981;
  font-size: 16px;
}

@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .features {
    padding: 60px 0;
  }
  
  .section-title {
    font-size: 28px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .feature-card {
    padding: 24px;
  }
}
</style>
