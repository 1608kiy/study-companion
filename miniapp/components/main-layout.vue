<template>
  <view class="main-layout">
    <view class="content" :class="{ 'has-tabbar': showTabbar }">
      <slot></slot>
    </view>
    <tab-bar v-if="showTabbar" :currentTab="currentTab" @change="onTabChange" />
  </view>
</template>

<script setup>
import { onMounted } from 'vue'
import TabBar from './tab-bar.vue'

const props = defineProps({
  showTabbar: {
    type: Boolean,
    default: true
  },
  currentTab: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['tabchange'])

const onTabChange = (index) => {
  emit('tabchange', index)
}

onMounted(() => {
  // 隐藏原生 tabBar
  if (props.showTabbar) {
    uni.hideTabBar()
  }
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: #f8fafc;
}

.content {
  min-height: 100vh;
}

/* 修复：使用 calc 动态计算安全区域 */
.content.has-tabbar {
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom, 0px));
}
</style>
