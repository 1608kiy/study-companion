<template>
  <el-card class="exam-countdown-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">考试倒计时</span>
        <el-button type="primary" link @click="showDialog = true">
          <el-icon><Plus /></el-icon>
          添加考试
        </el-button>
      </div>
    </template>

    <div v-if="!exams.length" class="empty-exam">
      <el-icon size="32" color="#94a3b8"><Calendar /></el-icon>
      <p>暂无考试安排</p>
    </div>

    <div v-else class="exam-list">
      <div v-for="exam in exams" :key="exam.id" :class="['exam-item', { urgent: exam.daysLeft <= 7 }]">
        <div class="exam-info">
          <h4>{{ exam.name }}</h4>
          <p class="exam-date">{{ exam.examDate }}</p>
          <p v-if="exam.location" class="exam-location">{{ exam.location }}</p>
        </div>
        <div class="exam-countdown">
          <span class="days">{{ exam.daysLeft }}</span>
          <span class="label">天</span>
        </div>
        <div class="exam-actions">
          <el-button icon="Edit" circle size="small" @click="editExam(exam)" />
          <el-button icon="Delete" circle size="small" type="danger" @click="handleDelete(exam)" />
        </div>
      </div>
    </div>

    <!-- 添加/编辑考试对话框 -->
    <el-dialog v-model="showDialog" :title="editingExam ? '编辑考试' : '添加考试'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="考试名称" prop="name">
          <el-input v-model="form.name" placeholder="如：国考、省考" />
        </el-form-item>
        <el-form-item label="考试日期" prop="examDate">
          <el-date-picker
            v-model="form.examDate"
            type="date"
            placeholder="选择考试日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="考试地点">
          <el-input v-model="form.location" placeholder="考试地点（选填）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="备注信息（选填）" />
        </el-form-item>
        <el-form-item label="目标考试">
          <el-switch v-model="form.isTarget" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editingExam ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { examApi } from '@/api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const exams = ref([])
const showDialog = ref(false)
const submitting = ref(false)
const editingExam = ref(null)

const formRef = ref(null)
const form = reactive({
  name: '',
  examDate: '',
  location: '',
  description: '',
  isTarget: false,
})

const rules = {
  name: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  examDate: [{ required: true, message: '请选择考试日期', trigger: 'change' }],
}

const loadExams = async () => {
  try {
    const res = await examApi.getList()
    exams.value = (res.data || []).sort((a, b) => a.daysLeft - b.daysLeft)
  } catch (error) {
    console.error('获取考试列表失败:', error)
  }
}

const editExam = (exam) => {
  editingExam.value = exam
  form.name = exam.name
  form.examDate = exam.examDate
  form.location = exam.location || ''
  form.description = exam.description || ''
  form.isTarget = exam.isTarget
  showDialog.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (editingExam.value) {
        await examApi.update(editingExam.value.id, form)
        ElMessage.success('更新成功')
      } else {
        await examApi.create(form)
        ElMessage.success('添加成功')
      }
      showDialog.value = false
      editingExam.value = null
      form.name = ''
      form.examDate = ''
      form.location = ''
      form.description = ''
      form.isTarget = false
      await loadExams()
    } catch (error) {
      console.error('操作失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (exam) => {
  try {
    await ElMessageBox.confirm('确定删除该考试？', '确认删除', { type: 'warning' })
    await examApi.delete(exam.id)
    ElMessage.success('删除成功')
    await loadExams()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(() => {
  loadExams()
})
</script>

<style scoped>
.exam-countdown-card { height: 100%; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title { font-weight: 600; color: var(--text-primary); }

.empty-exam {
  text-align: center;
  padding: 30px 0;
  color: var(--text-secondary);
}

.empty-exam p { margin: 8px 0 0; font-size: 14px; }

.exam-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.exam-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-page);
  border-radius: var(--radius);
  border-left: 4px solid var(--primary);
}

.exam-item.urgent {
  border-left-color: #ef4444;
  background: #fef2f2;
}

.exam-info {
  flex: 1;
}

.exam-info h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.exam-date {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.exam-location {
  font-size: 12px;
  color: var(--text-muted);
  margin: 2px 0 0;
}

.exam-countdown {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 48px;
}

.exam-countdown .days {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary);
  line-height: 1;
}

.exam-item.urgent .exam-countdown .days {
  color: #ef4444;
}

.exam-countdown .label {
  font-size: 12px;
  color: var(--text-muted);
}

.exam-actions {
  display: flex;
  gap: 4px;
}
</style>
