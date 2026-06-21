<template>
  <main-layout :showTabbar="false">
    <scroll-view class="page-scroll" scroll-y>
      <view class="page-container">
        <view class="page-header">
          <text class="page-title">学习资料</text>
          <button class="btn-sm" @click="showUpload = true">上传资料</button>
        </view>
        
        <!-- 筛选 -->
        <view class="filter-bar">
          <input 
            v-model="keyword" 
            class="search-input" 
            placeholder="搜索资料..." 
            @confirm="loadMaterials"
          />
        </view>
        
        <!-- 资料列表 -->
        <view v-if="!materials.length" class="empty-state">
          <text class="empty-icon">📁</text>
          <text class="empty-text">暂无学习资料</text>
        </view>
        
        <view v-else class="material-list">
          <view 
            v-for="item in materials" 
            :key="item.id" 
            class="material-item"
          >
            <view class="material-icon">
              <text class="file-icon">{{ getFileIcon(item.fileType) }}</text>
              <text class="file-type">{{ item.fileType?.toUpperCase() }}</text>
            </view>
            <view class="material-info">
              <text class="material-title">{{ item.title }}</text>
              <text class="material-meta" v-if="item.subjectName">{{ item.subjectName }} · {{ formatSize(item.fileSize) }}</text>
              <text class="material-meta" v-else>{{ formatSize(item.fileSize) }}</text>
            </view>
            <view class="material-actions">
              <text 
                :class="['action-btn', { active: item.isFavorite }]" 
                @click="toggleFavorite(item)"
              >
                {{ item.isFavorite ? '★' : '☆' }}
              </text>
              <text class="action-btn delete" @click="handleDelete(item)">✕</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 上传弹窗 -->
    <view v-if="showUpload" class="modal-overlay" @click="showUpload = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">上传学习资料</text>
        
        <view class="form-item">
          <text class="form-label">选择文件</text>
          <button class="btn-outline" @click="chooseFile">
            {{ selectedFile ? selectedFile.name : '点击选择文件' }}
          </button>
        </view>
        
        <view class="form-item">
          <text class="form-label">标题</text>
          <input v-model="uploadForm.title" class="form-input" placeholder="资料标题" />
        </view>
        
        <view class="form-item">
          <text class="form-label">标签</text>
          <input v-model="uploadForm.tags" class="form-input" placeholder="多个标签用逗号分隔" />
        </view>
        
        <view class="modal-btns">
          <button class="btn-cancel" @click="showUpload = false">取消</button>
          <button class="btn-primary" @click="handleUpload" :loading="uploading">上传</button>
        </view>
      </view>
    </view>
  </main-layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { materialApi } from '../../api/modules'
import MainLayout from '../../components/main-layout.vue'

const loading = ref(true)
const showUpload = ref(false)
const uploading = ref(false)
const materials = ref([])
const keyword = ref('')
const selectedFile = ref(null)

const uploadForm = reactive({
  title: '',
  tags: ''
})

const getFileIcon = (type) => {
  const icons = {
    pdf: '📄',
    doc: '📝',
    docx: '📝',
    xls: '📊',
    xlsx: '📊',
    ppt: '📽',
    pptx: '📽',
    jpg: '🖼',
    png: '🖼'
  }
  return icons[type?.toLowerCase()] || '📎'
}

const formatSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const loadMaterials = async () => {
  try {
    const params = {}
    if (keyword.value) params.keyword = keyword.value
    const res = await materialApi.getList(params)
    materials.value = res.data || []
  } catch (error) {
    uni.showToast({ title: '获取资料失败', icon: 'none' })
  }
}

const chooseFile = () => {
  uni.chooseFile({
    count: 1,
    success: (res) => {
      selectedFile.value = res.tempFiles[0]
      if (!uploadForm.title) {
        uploadForm.title = res.tempFiles[0].name.replace(/\.[^/.]+$/, '')
      }
    }
  })
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    uni.showToast({ title: '请选择文件', icon: 'none' })
    return
  }
  if (!uploadForm.title) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('title', uploadForm.title)
    if (uploadForm.tags) formData.append('tags', uploadForm.tags)
    
    await materialApi.upload(formData)
    uni.showToast({ title: '上传成功', icon: 'success' })
    showUpload.value = false
    selectedFile.value = null
    uploadForm.title = ''
    uploadForm.tags = ''
    await loadMaterials()
  } catch (error) {
    uni.showToast({ title: '上传失败', icon: 'none' })
  } finally {
    uploading.value = false
  }
}

const toggleFavorite = async (item) => {
  try {
    await materialApi.toggleFavorite(item.id)
    item.isFavorite = !item.isFavorite
  } catch (error) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const handleDelete = async (item) => {
  uni.showModal({
    title: '确认删除',
    content: '确定删除该资料？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await materialApi.delete(item.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          await loadMaterials()
        } catch (error) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  loadMaterials()
})
</script>

<style scoped>
.page-scroll {
  height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.page-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #1e293b;
}

.filter-bar {
  margin-bottom: 24rpx;
}

.search-input {
  width: 100%;
  height: 72rpx;
  padding: 0 24rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
  font-size: 28rpx;
}

.empty-state {
  text-align: center;
  padding: 80rpx 0;
}

.empty-icon {
  display: block;
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #64748b;
}

.material-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.material-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
}

.material-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.file-icon {
  font-size: 48rpx;
}

.file-type {
  font-size: 20rpx;
  color: #64748b;
}

.material-info {
  flex: 1;
}

.material-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4rpx;
}

.material-meta {
  display: block;
  font-size: 24rpx;
  color: #64748b;
}

.material-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  font-size: 36rpx;
  color: #94a3b8;
}

.action-btn.active {
  color: #f59e0b;
}

.action-btn.delete {
  font-size: 28rpx;
  color: #ef4444;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 85%;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.modal-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1e293b;
  text-align: center;
  margin-bottom: 30rpx;
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
  padding: 0 24rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.btn-outline {
  width: 100%;
  height: 80rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #64748b;
  border: 1rpx dashed #e2e8f0;
}

.modal-btns {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.btn-cancel {
  flex: 1;
  height: 80rpx;
  background: #f1f5f9;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #64748b;
}

.btn-primary {
  flex: 1;
  height: 80rpx;
  background: #6366f1;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #fff;
}
</style>
