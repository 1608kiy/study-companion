<template>
  <div class="admin-container">
    <div class="admin-header">
      <h1>管理后台</h1>
      <el-button @click="$router.push('/home')">返回主站</el-button>
    </div>

    <el-tabs v-model="activeTab" class="admin-tabs">
      <!-- 仪表盘 -->
      <el-tab-pane label="仪表盘" name="dashboard">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon blue"><el-icon size="24"><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon green"><el-icon size="24"><UserFilled /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.todayNewUsers || 0 }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon orange"><el-icon size="24"><Timer /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.todayStudyDuration || 0 }}</div>
              <div class="stat-label">今日学习(分钟)</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon purple"><el-icon size="24"><ChatDotRound /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.totalAiChats || 0 }}</div>
              <div class="stat-label">AI对话次数</div>
            </div>
          </div>
        </div>

        <el-card class="recent-users-card">
          <template #header>
            <span class="card-title">最近注册用户</span>
          </template>
          <el-table :data="dashboard.recentUsers || []" style="width: 100%">
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column prop="createTime" label="注册时间" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="users">
        <div class="table-header">
          <el-input
            v-model="userKeyword"
            placeholder="搜索用户..."
            prefix-icon="Search"
            clearable
            style="width: 300px"
            @input="loadUsers"
          />
        </div>

        <el-table :data="users.list || []" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'admin' ? 'danger' : 'success'" size="small">
                {{ row.role === 'admin' ? '管理员' : '用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="editUser(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteUser(row)" :disabled="row.role === 'admin'">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="userPage"
            :page-size="10"
            :total="users.total || 0"
            layout="prev, pager, next"
            @current-change="loadUsers"
          />
        </div>
      </el-tab-pane>

      <!-- AI 配置 -->
      <el-tab-pane label="AI 配置" name="ai">
        <el-card>
          <template #header>
            <span class="card-title">AI 服务配置</span>
          </template>
          <el-form :model="aiConfig" label-width="120px">
            <el-form-item label="AI 功能开关">
              <el-switch v-model="aiConfigEnabled" />
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="aiConfig['ai.api-key']" type="password" show-password placeholder="请输入 API Key" />
            </el-form-item>
            <el-form-item label="接口地址">
              <el-input v-model="aiConfig['ai.base-url']" placeholder="请输入接口地址" />
            </el-form-item>
            <el-form-item label="模型名称">
              <el-input v-model="aiConfig['ai.model']" placeholder="请输入模型名称" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveAiConfig" :loading="saving">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑用户" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { adminApi } from '@/api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('dashboard')
const dashboard = ref({})
const users = ref({})
const userPage = ref(1)
const userKeyword = ref('')
const showEditDialog = ref(false)
const saving = ref(false)

const aiConfig = reactive({
  'ai.api-key': '',
  'ai.base-url': '',
  'ai.model': '',
  'ai.enabled': 'true'
})

const aiConfigEnabled = computed({
  get: () => aiConfig['ai.enabled'] === 'true',
  set: (val) => { aiConfig['ai.enabled'] = val ? 'true' : 'false' }
})

const editForm = reactive({
  id: null,
  email: '',
  nickname: '',
  role: 'user'
})

const loadDashboard = async () => {
  try {
    const res = await adminApi.getDashboard()
    dashboard.value = res.data
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

const loadUsers = async () => {
  try {
    const res = await adminApi.getUsers({ page: userPage.value, size: 10, keyword: userKeyword.value })
    users.value = res.data
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

const loadAiConfig = async () => {
  try {
    const res = await adminApi.getAiConfig()
    Object.assign(aiConfig, res.data)
  } catch (error) {
    console.error('获取AI配置失败:', error)
  }
}

const editUser = (user) => {
  editForm.id = user.id
  editForm.email = user.email
  editForm.nickname = user.nickname
  editForm.role = user.role || 'user'
  showEditDialog.value = true
}

const saveUser = async () => {
  saving.value = true
  try {
    await adminApi.updateUser(editForm.id, {
      nickname: editForm.nickname,
      role: editForm.role
    })
    ElMessage.success('更新成功')
    showEditDialog.value = false
    await loadUsers()
  } catch (error) {
    console.error('更新用户失败:', error)
  } finally {
    saving.value = false
  }
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm('确定删除该用户？', '确认删除', { type: 'warning' })
    await adminApi.deleteUser(user.id)
    ElMessage.success('删除成功')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
    }
  }
}

const saveAiConfig = async () => {
  saving.value = true
  try {
    await adminApi.updateAiConfig(aiConfig)
    ElMessage.success('配置已保存')
  } catch (error) {
    console.error('保存AI配置失败:', error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadDashboard()
  loadUsers()
  loadAiConfig()
})
</script>

<style scoped>
.admin-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.admin-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: white;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.blue { background: var(--stat-blue); }
.stat-icon.green { background: var(--stat-green); }
.stat-icon.orange { background: var(--stat-orange); }
.stat-icon.purple { background: var(--stat-purple); }

.stat-info { flex: 1; }
.stat-value { font-size: 32px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 14px; color: var(--text-secondary); margin-top: 4px; }

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.card-title {
  font-weight: 600;
  color: var(--text-primary);
}

.recent-users-card {
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
