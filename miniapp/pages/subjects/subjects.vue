<template>
  <view class="subjects-container">
    <view class="header">
      <text class="title">科目管理</text>
      <button class="btn-add" @click="handleAdd">添加科目</button>
    </view>
    
    <!-- 骨架屏 -->
    <view v-if="loading" class="skeleton-wrapper">
      <view class="skeleton-item" v-for="i in 5" :key="i"></view>
    </view>
    
    <!-- 科目列表 -->
    <view v-else class="subject-list">
      <view v-if="subjects.length === 0" class="empty-state">
        <text class="empty-icon">📚</text>
        <text class="empty-text">暂无科目</text>
        <button class="btn-add-empty" @click="handleAdd">添加科目</button>
      </view>
      
      <view 
        v-for="subject in subjects" 
        :key="subject.id" 
        class="subject-item"
      >
        <view class="subject-info">
          <view class="subject-color" :style="{ background: subject.color || '#6366f1' }"></view>
          <text class="subject-name">{{ subject.name }}</text>
        </view>
        <view class="subject-actions">
          <text class="action-btn" @click="handleEdit(subject)">编辑</text>
          <text class="action-btn delete" @click="handleDelete(subject)">删除</text>
        </view>
      </view>
    </view>
    
    <!-- 添加/编辑弹窗 -->
    <view v-if="showDialog" class="dialog-mask" @click="closeDialog">
      <view class="dialog-content" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">{{ isEdit ? '编辑科目' : '添加科目' }}</text>
          <text class="dialog-close" @click="closeDialog">✕</text>
        </view>
        <view class="dialog-body">
          <view class="form-item">
            <text class="form-label">科目名称 *</text>
            <input 
              v-model="form.name" 
              class="form-input" 
              placeholder="请输入科目名称"
            />
          </view>
          <view class="form-item">
            <text class="form-label">颜色</text>
            <view class="color-picker">
              <view 
                v-for="color in colors" 
                :key="color"
                class="color-item"
                :class="{ active: form.color === color }"
                :style="{ background: color }"
                @click="form.color = color"
              ></view>
            </view>
          </view>
        </view>
        <view class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-confirm" @click="submitSubject" :loading="submitting">
            {{ isEdit ? '保存' : '添加' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { subjectApi } from '../../api/modules'

const loading = ref(true)
const submitting = ref(false)
const subjects = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const form = ref({
  name: '',
  color: '#6366f1'
})

const colors = [
  '#6366f1', '#3b82f6', '#10b981', '#f59e0b', '#ef4444',
  '#8b5cf6', '#ec4899', '#14b8a6', '#f97316', '#6b7280'
]

const loadSubjects = async () => {
  loading.value = true
  try {
    const res = await subjectApi.getList()
    subjects.value = res.data || []
  } catch (error) {
    uni.showToast({ title: '获取科目失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  form.value = { name: '', color: '#6366f1' }
  showDialog.value = true
}

const handleEdit = (subject) => {
  isEdit.value = true
  editingId.value = subject.id
  form.value = {
    name: subject.name,
    color: subject.color || '#6366f1'
  }
  showDialog.value = true
}

const closeDialog = () => {
  showDialog.value = false
}

const submitSubject = async () => {
  if (!form.value.name?.trim()) {
    uni.showToast({ title: '请输入科目名称', icon: 'none' })
    return
  }
  
  submitting.value = true
  try {
    if (isEdit.value && editingId.value) {
      await subjectApi.update(editingId.value, form.value)
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await subjectApi.create(form.value)
      uni.showToast({ title: '添加成功', icon: 'success' })
    }
    closeDialog()
    await loadSubjects()
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const handleDelete = (subject) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除科目"${subject.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await subjectApi.delete(subject.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          await loadSubjects()
        } catch (error) {
          uni.showToast({ title: error.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped>
.subjects-container {
  padding: 20rpx;
  background: #f8fafc;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1e293b;
}

.btn-add {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 24rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 12rpx;
  font-size: 26rpx;
  font-weight: 600;
}

/* 骨架屏 */
.skeleton-wrapper {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.skeleton-item {
  height: 100rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
}

/* 科目列表 */
.subject-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.subject-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
}

.subject-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.subject-color {
  width: 32rpx;
  height: 32rpx;
  border-radius: 8rpx;
}

.subject-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
}

.subject-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  font-size: 26rpx;
  color: #6366f1;
}

.action-btn.delete {
  color: #ef4444;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 100rpx 40rpx;
}

.empty-icon {
  display: block;
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  display: block;
  font-size: 28rpx;
  color: #94a3b8;
  margin-bottom: 40rpx;
}

.btn-add-empty {
  width: 240rpx;
  height: 80rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
}

/* 弹窗 */
.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  width: 90%;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #e2e8f0;
}

.dialog-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1e293b;
}

.dialog-close {
  font-size: 32rpx;
  color: #64748b;
  padding: 10rpx;
}

.dialog-body {
  padding: 24rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 12rpx;
}

.form-input {
  width: 100%;
  height: 80rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  border: 1rpx solid #e2e8f0;
}

.color-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.color-item {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
}

.color-item.active {
  border-color: #1e293b;
}

.dialog-footer {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
  border-top: 1rpx solid #e2e8f0;
}

.btn-cancel {
  flex: 1;
  height: 80rpx;
  background: #f8fafc;
  color: #64748b;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.btn-confirm {
  flex: 1;
  height: 80rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
}
</style>
