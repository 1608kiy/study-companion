<template>
  <view class="main-layout">
    <view class="content" :class="{ 'has-tabbar': showTabbar }">
      <slot></slot>
    </view>
    <tab-bar v-if="showTabbar" :currentTab="currentTab" @change="onTabChange" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
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
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: #f8fafc;
}

.content {
  min-height: 100vh;
}

.content.has-tabbar {
  padding-bottom: 120rpx;
}
</style>
