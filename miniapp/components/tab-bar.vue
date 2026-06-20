<template>
  <view class="tab-bar">
    <view 
      v-for="(item, index) in tabList" 
      :key="index"
      class="tab-item"
      :class="{ active: currentTab === index }"
      @click="switchTab(index)"
    >
      <view class="tab-icon">
        <text class="icon-text">{{ item.icon }}</text>
      </view>
      <text class="tab-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'

const props = defineProps({
  currentTab: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['change'])

const tabList = [
  {
    pagePath: '/pages/home/home',
    text: '首页',
    icon: '🏠'
  },
  {
    pagePath: '/pages/study/study',
    text: '学习',
    icon: '⏱️'
  },
  {
    pagePath: '/pages/diary/diary',
    text: '日记',
    icon: '📝'
  },
  {
    pagePath: '/pages/stats/stats',
    text: '统计',
    icon: '📊'
  },
  {
    pagePath: '/pages/ai/ai',
    text: 'AI',
    icon: '🤖'
  }
]

const switchTab = (index) => {
  const item = tabList[index]
  uni.switchTab({
    url: item.pagePath
  })
  emit('change', index)
}
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-around;
  border-top: 1rpx solid #e2e8f0;
  padding-bottom: env(safe-area-inset-bottom, 0);
  z-index: 999;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: 8rpx 0;
}

.tab-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-text {
  font-size: 36rpx;
}

.tab-text {
  font-size: 20rpx;
  color: #64748b;
}

.tab-item.active .tab-text {
  color: #6366f1;
}
</style>
