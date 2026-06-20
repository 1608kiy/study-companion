<template>
  <div class="diary-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span class="card-title">我的日记</span>
              <el-button type="primary" @click="showCreateDialog">
                <el-icon><Plus /></el-icon>
                写日记
              </el-button>
            </div>
          </template>
          
          <el-calendar v-model="selectedDate" class="diary-calendar">
            <template #date-cell="{ data }">
              <div 
                class="calendar-day" 
                :class="{ 'has-diary': hasDiary(data.day) }"
                @click="selectDate(data.day)"
              >
                {{ data.day.split('-')[2] }}
              </div>
            </template>
          </el-calendar>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card v-if="currentDiary" class="diary-detail">
          <template #header>
            <div class="card-header">
              <span class="card-title">{{ formatDiaryDate(currentDiary.diaryDate) }}</span>
              <el-dropdown @command="handleCommand">
                <el-icon class="more-btn"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="regenerate" :disabled="currentDiary.aiGenerateCount >= 3">
                      AI重新生成
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          
          <div class="diary-content" v-html="renderMarkdown(currentDiary.content)"></div>
          
          <div v-if="currentDiary.images && currentDiary.images.length" class="diary-images">
            <div class="image-list">
              <div v-for="image in currentDiary.images" :key="image.id" class="image-item">
                <img :src="image.imageUrl" :alt="'图片' + image.sortOrder" />
              </div>
            </div>
          </div>
          
          <div class="diary-footer">
            <el-tag v-if="currentDiary.aiGenerated" type="success" size="small" effect="plain">
              <el-icon><MagicStick /></el-icon>
              AI生成
            </el-tag>
            <span class="generate-count">生成次数: {{ currentDiary.aiGenerateCount }}/3</span>
          </div>
        </el-card>
        
        <el-card v-else class="empty-card">
          <el-empty description="选择日期查看日记" />
        </el-card>
      </el-col>
    </el-row>
    
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑日记' : '写日记'" 
      width="600px"
      destroy-on-close
    >
      <el-form :model="diaryForm" label-width="80px">
        <el-form-item label="日记内容">
          <el-input 
            v-model="diaryForm.content" 
            type="textarea" 
            :rows="10" 
            placeholder="记录今天的学习心得..."
          />
        </el-form-item>
        <el-form-item label="今日总结">
          <el-input v-model="diaryForm.summary" placeholder="今天学到了什么？" />
        </el-form-item>
        <el-form-item label="明日计划">
          <el-input v-model="diaryForm.plan" placeholder="明天打算做什么？" />
        </el-form-item>
        <el-form-item label="心得体会">
          <el-input v-model="diaryForm.reflection" placeholder="有什么感悟？" />
        </el-form-item>
        <el-form-item label="问题记录">
          <el-input v-model="diaryForm.problems" placeholder="遇到了什么问题？" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDiary" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { diaryApi } from '@/api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const selectedDate = ref(new Date())
const diaryList = ref([])
const currentDiary = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)

const diaryForm = ref({
  content: '',
  summary: '',
  plan: '',
  reflection: '',
  problems: '',
})

const diaryDates = computed(() => {
  return diaryList.value.map(d => d.diaryDate)
})

const hasDiary = (date) => {
  return diaryDates.value.includes(date)
}

const formatDiaryDate = (date) => {
  return dayjs(date).format('YYYY年MM月DD日')
}

const renderMarkdown = (content) => {
  if (!content) return ''
  return content
    .replace(/### (.*)/g, '<h3>$1</h3>')
    .replace(/## (.*)/g, '<h2>$1</h2>')
    .replace(/# (.*)/g, '<h1>$1</h1>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

const selectDate = async (date) => {
  selectedDate.value = new Date(date)
  await loadDiary(date)
}

const loadDiaryList = async () => {
  try {
    const month = dayjs(selectedDate.value).format('YYYY-MM')
    const res = await diaryApi.getList({ month })
    diaryList.value = res.data || []
  } catch (error) {
    console.error('获取日记列表失败:', error)
  }
}

const loadDiary = async (date) => {
  try {
    const res = await diaryApi.getByDate(date)
    currentDiary.value = res.data
  } catch (error) {
    currentDiary.value = null
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  diaryForm.value = {
    content: '',
    summary: '',
    plan: '',
    reflection: '',
    problems: '',
  }
  dialogVisible.value = true
}

const submitDiary = async () => {
  if (!diaryForm.value.content) {
    ElMessage.warning('请输入日记内容')
    return
  }
  
  submitting.value = true
  try {
    if (isEdit.value && currentDiary.value) {
      await diaryApi.update(currentDiary.value.id, diaryForm.value)
      ElMessage.success('保存成功')
    } else {
      await diaryApi.create(diaryForm.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadDiaryList()
    await loadDiary(dayjs(selectedDate.value).format('YYYY-MM-DD'))
  } catch (error) {
    console.error('保存日记失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleCommand = async (command) => {
  if (command === 'edit') {
    isEdit.value = true
    diaryForm.value = { ...currentDiary.value }
    dialogVisible.value = true
  } else if (command === 'regenerate') {
    try {
      await ElMessageBox.confirm('确定要重新生成日记吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
      await diaryApi.regenerate(currentDiary.value.id)
      ElMessage.success('重新生成成功')
      await loadDiary(dayjs(selectedDate.value).format('YYYY-MM-DD'))
    } catch (error) {
      if (error !== 'cancel') {
        console.error('重新生成失败:', error)
      }
    }
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这篇日记吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
      await diaryApi.delete(currentDiary.value.id)
      ElMessage.success('删除成功')
      currentDiary.value = null
      await loadDiaryList()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
      }
    }
  }
}

onMounted(async () => {
  await loadDiaryList()
  await loadDiary(dayjs(selectedDate.value).format('YYYY-MM-DD'))
})
</script>

<style scoped>
.diary-container {
  padding: 0;
}

.card-title {
  font-weight: 600;
  color: #1e293b;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.diary-calendar {
  height: 400px;
}

.calendar-day {
  height: 100%;
  padding: 8px;
  cursor: pointer;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.calendar-day:hover {
  background-color: #f1f5f9;
}

.calendar-day.has-diary {
  background-color: #eef2ff;
  font-weight: 600;
  color: #6366f1;
}

.diary-detail {
  min-height: 500px;
}

.more-btn {
  cursor: pointer;
  font-size: 18px;
  color: #64748b;
  transition: color 0.2s;
}

.more-btn:hover {
  color: #1e293b;
}

.diary-content {
  line-height: 1.8;
  color: #334155;
  margin-bottom: 20px;
  font-size: 14px;
}

.diary-content :deep(h1) {
  font-size: 22px;
  margin-bottom: 14px;
  color: #1e293b;
}

.diary-content :deep(h2) {
  font-size: 18px;
  margin-bottom: 10px;
  color: #1e293b;
}

.diary-content :deep(h3) {
  font-size: 15px;
  margin-bottom: 8px;
  color: #1e293b;
}

.diary-images {
  margin-top: 20px;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.image-item img {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.diary-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.generate-count {
  font-size: 12px;
  color: #94a3b8;
}

.empty-card {
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
