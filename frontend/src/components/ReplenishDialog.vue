<template>
  <el-dialog v-model="visible" title="补卡申请" width="480px" :close-on-click-modal="false">
    <!-- 步骤1: 选择断签日期并填写原因 -->
    <div v-if="step === 1">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="断签日期" prop="missDate">
          <el-date-picker
            v-model="form.missDate"
            type="date"
            placeholder="选择断签日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disableFutureDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="断签原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="3"
            placeholder="请说明断签原因（如：生病、出差、考试等）"
          />
        </el-form-item>
      </el-form>
      <div class="tip">
        <el-icon><InfoFilled /></el-icon>
        <span>AI 将根据您的原因判断是否允许补签</span>
      </div>
    </div>

    <!-- 步骤2: AI判断结果 -->
    <div v-if="step === 2" class="result-section">
      <div v-if="judging" class="judging">
        <el-icon class="loading-icon" size="32"><Loading /></el-icon>
        <p>AI 正在评估您的补签申请...</p>
      </div>
      <div v-else-if="judgmentResult" class="judgment-result">
        <div :class="['result-icon', judgmentResult.allow ? 'success' : 'fail']">
          <el-icon size="48">
            <CircleCheck v-if="judgmentResult.allow" />
            <CircleClose v-else />
          </el-icon>
        </div>
        <h3>{{ judgmentResult.allow ? '允许补签' : '不允许补签' }}</h3>
        <p class="reason">{{ judgmentResult.reason }}</p>
      </div>
    </div>

    <!-- 步骤3: 补签成功 -->
    <div v-if="step === 3" class="success-section">
      <div class="success-icon">
        <el-icon size="64" color="#10b981"><CircleCheck /></el-icon>
      </div>
      <h3>补签成功！</h3>
      <p>{{ form.missDate }} 的打卡已补签</p>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="step === 1" @click="visible = false">取消</el-button>
        <el-button v-if="step === 1" type="primary" :loading="submitting" @click="handleSubmit">
          提交申请
        </el-button>
        <el-button v-if="step === 2 && judgmentResult?.allow" type="primary" :loading="replenishing" @click="handleReplenish">
          确认补签
        </el-button>
        <el-button v-if="step === 2" @click="step = 1">返回</el-button>
        <el-button v-if="step === 3" type="primary" @click="handleDone">完成</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { checkInApi } from '@/api/modules'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const step = ref(1)
const submitting = ref(false)
const judging = ref(false)
const replenishing = ref(false)
const judgmentResult = ref(null)
const currentMissRecordId = ref(null)

const formRef = ref(null)
const form = reactive({
  missDate: '',
  reason: '',
})

const rules = {
  missDate: [{ required: true, message: '请选择断签日期', trigger: 'change' }],
  reason: [{ required: true, message: '请填写断签原因', trigger: 'blur' }],
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    step.value = 1
    judgmentResult.value = null
    currentMissRecordId.value = null
    form.missDate = ''
    form.reason = ''
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const disableFutureDate = (date) => {
  return date > new Date()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      // 1. 记录断签
      const missRes = await checkInApi.recordMiss({
        missDate: form.missDate,
        reason: form.reason,
      })
      currentMissRecordId.value = missRes.data.id

      // 2. AI判断
      step.value = 2
      judging.value = true
      const judgeRes = await checkInApi.aiJudge(currentMissRecordId.value)
      judgmentResult.value = {
        allow: judgeRes.data.aiAllowReplenish,
        reason: judgeRes.data.reason || (judgeRes.data.aiAllowReplenish ? '符合补签条件' : '不符合补签条件'),
      }
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitting.value = false
      judging.value = false
    }
  })
}

const handleReplenish = async () => {
  replenishing.value = true
  try {
    await checkInApi.replenish(currentMissRecordId.value)
    step.value = 3
    emit('success')
  } catch (error) {
    console.error('补签失败:', error)
  } finally {
    replenishing.value = false
  }
}

const handleDone = () => {
  visible.value = false
}
</script>

<style scoped>
.tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  padding: 12px;
  background: var(--bg-page);
  border-radius: var(--radius);
}

.result-section,
.success-section {
  text-align: center;
  padding: 24px 0;
}

.judging {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.judgment-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.result-icon.success { color: #10b981; }
.result-icon.fail { color: #ef4444; }

.judgment-result h3 {
  font-size: 18px;
  color: var(--text-primary);
  margin: 0;
}

.judgment-result .reason {
  color: var(--text-secondary);
  font-size: 14px;
}

.success-section h3 {
  font-size: 20px;
  color: #10b981;
  margin: 16px 0 8px;
}

.success-section p {
  color: var(--text-secondary);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
