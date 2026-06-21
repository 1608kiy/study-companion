<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <img src="@/assets/logo.svg" alt="Logo" />
        </div>
        <h1>智学伴</h1>
        <p>AI学习陪伴平台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth-btn" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        <div class="auth-links">
          <span class="forgot-password">忘记密码？请联系管理员</span>
        </div>
        <div class="auth-register">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  email: '',
  password: '',
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await userStore.login(form)
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error) {
      console.error('登录失败:', error)
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
  color: var(--text-secondary);
  font-size: 14px;
}

.auth-links {
  margin-bottom: 12px;
}

.forgot-password {
  color: var(--text-muted);
  font-size: 13px;
}

.auth-register a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

.auth-register a:hover {
  text-decoration: underline;
}
</style>
