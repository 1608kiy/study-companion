<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <img src="@/assets/logo.svg" alt="Logo" />
        </div>
        <h1>重置密码</h1>
        <p>输入邮箱获取验证码</p>
      </div>

      <!-- 步骤1: 输入邮箱 -->
      <el-form v-if="step === 1" ref="emailFormRef" :model="emailForm" :rules="emailRules" label-width="0" size="large">
        <el-form-item prop="email">
          <el-input v-model="emailForm.email" placeholder="请输入注册邮箱" prefix-icon="Message" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth-btn" :loading="loading" @click="handleSendCode">
            获取验证码
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 步骤2: 输入验证码和新密码 -->
      <el-form v-else ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="0" size="large">
        <div class="code-tip">
          验证码已发送至 <strong>{{ emailForm.email }}</strong>
          <p class="code-hint">（开发环境：验证码显示在接口返回中）</p>
        </div>
        <el-form-item prop="code">
          <el-input v-model="resetForm.code" placeholder="请输入6位验证码" prefix-icon="Key" maxlength="6" />
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="resetForm.confirmPassword" type="password" placeholder="请确认新密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth-btn" :loading="loading" @click="handleResetPassword">
            重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <router-link to="/login" class="back-link">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/modules'
import { ElMessage } from 'element-plus'

const router = useRouter()

const step = ref(1)
const emailFormRef = ref(null)
const resetFormRef = ref(null)
const loading = ref(false)

const emailForm = reactive({
  email: '',
})

const resetForm = reactive({
  code: '',
  newPassword: '',
  confirmPassword: '',
})

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleSendCode = async () => {
  if (!emailFormRef.value) return

  await emailFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await authApi.forgotPassword({ email: emailForm.email })
      ElMessage.success('验证码已发送')
      // 开发环境：显示验证码
      if (res.data) {
        ElMessage.info(`开发环境验证码: ${res.data}`)
      }
      step.value = 2
    } catch (error) {
      console.error('发送验证码失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const handleResetPassword = async () => {
  if (!resetFormRef.value) return

  await resetFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await authApi.resetPassword({
        email: emailForm.email,
        code: resetForm.code,
        newPassword: resetForm.newPassword,
      })
      ElMessage.success('密码重置成功，请登录')
      router.push('/login')
    } catch (error) {
      console.error('重置密码失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
}

.auth-card {
  max-width: 400px;
  width: 100%;
  padding: 40px;
  background: white;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
}

@media (max-width: 480px) {
  .auth-card {
    padding: 24px;
    margin: 0 16px;
    border: none;
  }
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
}

.auth-logo img {
  width: 100%;
  height: 100%;
}

.auth-header h1 {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.auth-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.auth-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
}

.back-link {
  color: var(--primary);
  text-decoration: none;
  font-size: 14px;
}

.back-link:hover {
  text-decoration: underline;
}

.code-tip {
  text-align: center;
  margin-bottom: 16px;
  color: var(--text-secondary);
  font-size: 14px;
}

.code-hint {
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 4px;
}
</style>
