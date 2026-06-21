<template>
  <div class="materials-container">
    <div class="page-header">
      <h2>学习资料</h2>
      <el-button type="primary" @click="showUpload = true">
        <el-icon><Upload /></el-icon>
        上传资料
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索资料..."
        prefix-icon="Search"
        clearable
        style="width: 200px"
        @input="handleSearch"
      />
      <el-select v-model="subjectFilter" placeholder="按科目筛选" clearable @change="loadMaterials">
        <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-checkbox v-model="favoriteOnly" @change="loadMaterials">仅收藏</el-checkbox>
    </div>

    <!-- 资料列表 -->
    <div v-if="loading" class="skeleton-wrapper">
      <div v-for="i in 6" :key="i" class="skeleton-card"></div>
    </div>

    <div v-else-if="!materials.length" class="empty-state">
      <el-icon size="48" color="#94a3b8"><Folder /></el-icon>
      <p>暂无学习资料</p>
      <el-button type="primary" @click="showUpload = true">上传第一份资料</el-button>
    </div>

    <div v-else class="materials-grid">
      <el-card v-for="item in materials" :key="item.id" class="material-card">
        <div class="material-icon">
          <el-icon size="32" :color="getFileColor(item.fileType)">
            <Document />
          </el-icon>
          <span class="file-type">{{ item.fileType?.toUpperCase() }}</span>
        </div>
        <div class="material-info">
          <h4>{{ item.title }}</h4>
          <p class="material-meta">
            <span v-if="item.subjectName" class="subject-tag">{{ item.subjectName }}</span>
            <span>{{ formatSize(item.fileSize) }}</span>
          </p>
          <p v-if="item.description" class="material-desc">{{ item.description }}</p>
          <div v-if="item.tags" class="material-tags">
            <el-tag v-for="tag in item.tags.split(',')" :key="tag" size="small">{{ tag.trim() }}</el-tag>
          </div>
        </div>
        <div class="material-actions">
          <el-button :icon="item.isFavorite ? 'StarFilled' : 'Star'" circle @click="toggleFavorite(item)" />
          <el-button icon="Download" circle @click="downloadFile(item)" />
          <el-button icon="Delete" circle type="danger" @click="handleDelete(item)" />
        </div>
      </el-card>
    </div>

    <!-- 上传对话框 -->
    <el-dialog v-model="showUpload" title="上传学习资料" width="500px">
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="80px">
        <el-form-item label="文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            drag
          >
            <el-icon size="40"><Upload /></el-icon>
            <div>点击或拖拽文件到此处</div>
            <template #tip>
              <div class="upload-tip">支持 PDF、Word、Excel、PPT、图片等格式，最大 10MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入资料标题" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="uploadForm.subjectId" placeholder="选择科目" clearable>
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" :rows="2" placeholder="添加描述..." />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="uploadForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUpload = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { materialApi, subjectApi } from '@/api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(true)
const showUpload = ref(false)
const uploading = ref(false)
const materials = ref([])
const subjects = ref([])
const keyword = ref('')
const subjectFilter = ref(null)
const favoriteOnly = ref(false)

const uploadFormRef = ref(null)
const uploadRef = ref(null)
const selectedFile = ref(null)

const uploadForm = reactive({
  title: '',
  subjectId: null,
  description: '',
  tags: '',
})

const uploadRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

const getFileColor = (type) => {
  const colors = {
    pdf: '#ef4444',
    doc: '#3b82f6',
    docx: '#3b82f6',
    xls: '#10b981',
    xlsx: '#10b981',
    ppt: '#f59e0b',
    pptx: '#f59e0b',
    jpg: '#8b5cf6',
    png: '#8b5cf6',
  }
  return colors[type?.toLowerCase()] || '#6366f1'
}

const formatSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const handleSearch = () => {
  loadMaterials()
}

const loadMaterials = async () => {
  try {
    const params = {}
    if (subjectFilter.value) params.subjectId = subjectFilter.value
    if (keyword.value) params.keyword = keyword.value
    const res = await materialApi.getList(params)
    materials.value = (res.data || []).filter(item => !favoriteOnly.value || item.isFavorite)
  } catch (error) {
    console.error('获取资料列表失败:', error)
  }
}

const loadSubjects = async () => {
  try {
    const res = await subjectApi.getList()
    subjects.value = res.data || []
  } catch (error) {
    console.error('获取科目列表失败:', error)
  }
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  if (!uploadForm.title) {
    uploadForm.title = file.name.replace(/\.[^/.]+$/, '')
  }
}

const handleFileRemove = () => {
  selectedFile.value = null
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  await uploadFormRef.value.validate(async (valid) => {
    if (!valid) return

    uploading.value = true
    try {
      const formData = new FormData()
      formData.append('file', selectedFile.value)
      formData.append('title', uploadForm.title)
      if (uploadForm.subjectId) formData.append('subjectId', uploadForm.subjectId)
      if (uploadForm.description) formData.append('description', uploadForm.description)
      if (uploadForm.tags) formData.append('tags', uploadForm.tags)

      await materialApi.upload(formData)
      ElMessage.success('上传成功')
      showUpload.value = false
      uploadForm.title = ''
      uploadForm.subjectId = null
      uploadForm.description = ''
      uploadForm.tags = ''
      selectedFile.value = null
      uploadRef.value?.clearFiles()
      await loadMaterials()
    } catch (error) {
      console.error('上传失败:', error)
    } finally {
      uploading.value = false
    }
  })
}

const toggleFavorite = async (item) => {
  try {
    await materialApi.toggleFavorite(item.id)
    item.isFavorite = !item.isFavorite
  } catch (error) {
    console.error('切换收藏失败:', error)
  }
}

const downloadFile = (item) => {
  window.open(item.fileUrl, '_blank')
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定删除该资料？', '确认删除', { type: 'warning' })
    await materialApi.delete(item.id)
    ElMessage.success('删除成功')
    await loadMaterials()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(async () => {
  try {
    await Promise.all([loadMaterials(), loadSubjects()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.materials-container { padding: 0; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 { font-size: 24px; font-weight: 700; color: var(--text-primary); }

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.materials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.material-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.material-icon {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-type {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 600;
}

.material-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.material-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.subject-tag {
  background: var(--primary-light);
  color: var(--primary);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.material-desc {
  font-size: 13px;
  color: var(--text-muted);
  margin: 4px 0 0;
}

.material-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.material-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-secondary);
}

.empty-state p { margin: 16px 0; }

.upload-tip {
  font-size: 12px;
  color: var(--text-muted);
}

.skeleton-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.skeleton-card {
  height: 160px;
  background: var(--border);
  border-radius: var(--radius-lg);
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
