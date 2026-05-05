<template>
  <div class="profile-view">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="page-subtitle">管理你的账号信息与偏好设置</p>
    </div>

    <div class="profile-layout">
      <!-- 左侧：用户卡片 -->
      <div class="profile-sidebar">
        <div class="card avatar-card">
          <div class="avatar-circle">{{ (userStore.nickname || 'U')[0].toUpperCase() }}</div>
          <div class="profile-name">{{ userStore.nickname }}</div>
          <div class="profile-username text-muted">@{{ userStore.username }}</div>
          <div class="profile-meta">
            <span class="meta-item">📧 {{ profileData.email || '未设置' }}</span>
            <span class="meta-item">📱 {{ profileData.phone || '未设置' }}</span>
          </div>
          <div v-if="profileData.signature" class="profile-signature">
            "{{ profileData.signature }}"
          </div>
        </div>

        <!-- 快捷导出 -->
        <div class="card export-card">
          <div class="export-title">📦 数据备份</div>
          <p class="export-desc">导出所有数据为 JSON 格式，安全备份你的每一天。</p>
          <el-button type="primary" :loading="exporting" @click="handleExport" class="export-btn">
            导出全量数据
          </el-button>
        </div>
      </div>

      <!-- 右侧：编辑区 -->
      <div class="profile-main">
        <!-- Tabs -->
        <el-tabs v-model="activeTab" class="profile-tabs">

          <!-- Tab 1: 基本信息 -->
          <el-tab-pane label="基本信息" name="info">
            <div class="card tab-card">
              <el-form :model="infoForm" label-width="80px" label-position="left">
                <el-form-item label="昵称">
                  <el-input v-model="infoForm.nickname" placeholder="设置你的昵称" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="infoForm.email" placeholder="your@email.com" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="infoForm.phone" placeholder="13800000000" />
                </el-form-item>
                <el-form-item label="个人签名">
                  <el-input
                    v-model="infoForm.signature"
                    type="textarea"
                    :rows="3"
                    placeholder="一句话介绍自己..."
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="savingInfo" @click="handleSaveInfo">
                    保存信息
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- Tab 2: 修改密码 -->
          <el-tab-pane label="修改密码" name="password">
            <div class="card tab-card">
              <el-form :model="pwdForm" label-width="100px" label-position="left">
                <el-form-item label="当前密码">
                  <el-input
                    v-model="pwdForm.oldPassword"
                    type="password"
                    show-password
                    placeholder="输入当前密码"
                  />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input
                    v-model="pwdForm.newPassword"
                    type="password"
                    show-password
                    placeholder="至少6位字符"
                  />
                </el-form-item>
                <el-form-item label="确认新密码">
                  <el-input
                    v-model="pwdForm.confirmPassword"
                    type="password"
                    show-password
                    placeholder="再次输入新密码"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="savingPwd" @click="handleChangePassword">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- Tab 3: 偏好设置 -->
          <el-tab-pane label="偏好设置" name="settings">
            <div class="card tab-card">
              <el-form :model="settingsForm" label-width="120px" label-position="left">
                <el-form-item label="界面主题">
                  <el-radio-group v-model="settingsForm.theme">
                    <el-radio-button label="dark">🌙 深色</el-radio-button>
                    <el-radio-button label="light">☀️ 浅色</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="默认首页">
                  <el-select v-model="settingsForm.defaultView" style="width: 200px">
                    <el-option label="数据看板" value="dashboard" />
                    <el-option label="每日计划" value="plan" />
                    <el-option label="每日摘录" value="excerpt" />
                    <el-option label="每日记账" value="accounting" />
                    <el-option label="每日总结" value="summary" />
                  </el-select>
                </el-form-item>
                <el-form-item label="一周起始日">
                  <el-radio-group v-model="settingsForm.weekStartDay">
                    <el-radio-button :label="1">周一</el-radio-button>
                    <el-radio-button :label="7">周日</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="每日提醒时间">
                  <el-time-picker
                    v-model="settingsForm.reminderTime"
                    format="HH:mm"
                    value-format="HH:mm:ss"
                    placeholder="选择提醒时间"
                    style="width: 160px"
                  />
                </el-form-item>
                <el-form-item label="语言">
                  <el-select v-model="settingsForm.language" style="width: 160px">
                    <el-option label="简体中文" value="zh-CN" />
                    <el-option label="English" value="en-US" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="savingSettings" @click="handleSaveSettings">
                    保存设置
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- Tab 4: 账号安全 -->
          <el-tab-pane label="账号安全" name="security">
            <div class="card tab-card security-card">
              <div class="security-item">
                <div class="security-info">
                  <div class="security-label">用户名</div>
                  <div class="security-value">{{ userStore.username }}</div>
                </div>
                <el-tag type="success" size="small">已设置</el-tag>
              </div>
              <el-divider />
              <div class="security-item">
                <div class="security-info">
                  <div class="security-label">登录密码</div>
                  <div class="security-value text-muted">定期修改密码保障账号安全</div>
                </div>
                <el-button size="small" @click="activeTab = 'password'">去修改</el-button>
              </div>
              <el-divider />
              <div class="security-item">
                <div class="security-info">
                  <div class="security-label">账户状态</div>
                  <div class="security-value">正常使用中</div>
                </div>
                <el-tag type="success" size="small">正常</el-tag>
              </div>
              <el-divider />
              <div class="danger-zone">
                <div class="danger-title">⚠️ 危险操作</div>
                <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
              </div>
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const userStore = useUserStore()
const router = useRouter()

const activeTab = ref('info')
const profileData = ref<any>({})

// 表单数据
const infoForm = reactive({ nickname: '', email: '', phone: '', signature: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const settingsForm = reactive({
  theme: 'dark',
  defaultView: 'dashboard',
  weekStartDay: 1,
  reminderTime: '21:00:00',
  language: 'zh-CN'
})

// 加载状态
const savingInfo = ref(false)
const savingPwd = ref(false)
const savingSettings = ref(false)
const exporting = ref(false)

async function loadProfile() {
  try {
    const data = await userApi.getProfile()
    profileData.value = data
    infoForm.nickname = data.nickname || ''
    infoForm.email = data.email || ''
    infoForm.phone = data.phone || ''
    infoForm.signature = data.signature || ''
  } catch {}
}

async function loadSettings() {
  try {
    const s = await userApi.getSettings()
    settingsForm.theme = s.theme || 'dark'
    settingsForm.defaultView = s.defaultView || 'dashboard'
    settingsForm.weekStartDay = s.weekStartDay ?? 1
    settingsForm.reminderTime = s.reminderTime || '21:00:00'
    settingsForm.language = s.language || 'zh-CN'
  } catch {}
}

async function handleSaveInfo() {
  savingInfo.value = true
  try {
    await userApi.updateProfile({
      nickname: infoForm.nickname,
      email: infoForm.email,
      phone: infoForm.phone,
      signature: infoForm.signature
    })
    userStore.updateUserInfo({ nickname: infoForm.nickname })
    await loadProfile()
    ElMessage.success('信息已保存')
  } finally {
    savingInfo.value = false
  }
}

async function handleChangePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次新密码输入不一致')
    return
  }
  savingPwd.value = true
  try {
    await userApi.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 1500)
  } finally {
    savingPwd.value = false
  }
}

async function handleSaveSettings() {
  savingSettings.value = true
  try {
    await userApi.updateSettings({
      theme: settingsForm.theme,
      defaultView: settingsForm.defaultView,
      weekStartDay: settingsForm.weekStartDay,
      reminderTime: settingsForm.reminderTime,
      language: settingsForm.language
    })
    ElMessage.success('偏好设置已保存')
  } finally {
    savingSettings.value = false
  }
}

async function handleExport() {
  exporting.value = true
  try {
    const blob = await userApi.exportData()
    const url = URL.createObjectURL(new Blob([blob], { type: 'application/json' }))
    const link = document.createElement('a')
    const today = new Date().toISOString().split('T')[0]
    link.href = url
    link.download = `daily-tracker-export-${today}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('数据导出成功！')
  } catch {
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}

async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadProfile()
  loadSettings()
})
</script>

<style lang="scss" scoped>
.profile-view {
  .profile-layout {
    display: grid;
    grid-template-columns: 280px 1fr;
    gap: 20px;
    align-items: start;

    @media (max-width: 900px) {
      grid-template-columns: 1fr;
    }
  }

  /* 左侧卡片 */
  .profile-sidebar {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .avatar-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      padding: 28px 20px;
      text-align: center;

      .avatar-circle {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        background: linear-gradient(135deg, $primary, #7c3aed);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 32px;
        font-weight: 700;
        color: white;
        box-shadow: 0 8px 24px rgba($primary, 0.4);
      }

      .profile-name {
        font-size: 18px;
        font-weight: 700;
        color: $text-primary;
      }

      .profile-username {
        font-size: 13px;
      }

      .profile-meta {
        display: flex;
        flex-direction: column;
        gap: 4px;
        width: 100%;
        margin-top: 4px;

        .meta-item {
          font-size: 12px;
          color: $text-secondary;
          background: $bg-input;
          padding: 4px 10px;
          border-radius: $radius-sm;
        }
      }

      .profile-signature {
        font-size: 12px;
        color: $text-muted;
        font-style: italic;
        padding: 8px 12px;
        border-left: 2px solid rgba($primary, 0.5);
        text-align: left;
        width: 100%;
      }
    }

    .export-card {
      padding: 20px;

      .export-title {
        font-size: 14px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: 8px;
      }

      .export-desc {
        font-size: 12px;
        color: $text-muted;
        line-height: 1.6;
        margin-bottom: 12px;
      }

      .export-btn {
        width: 100%;
      }
    }
  }

  /* 右侧主区域 */
  .profile-main {
    .profile-tabs {
      :deep(.el-tabs__header) {
        margin-bottom: 0;
      }

      :deep(.el-tabs__nav-wrap::after) {
        display: none;
      }

      :deep(.el-tabs__item) {
        color: $text-secondary;
        font-size: 14px;

        &.is-active {
          color: $primary-light;
        }
      }

      :deep(.el-tabs__active-bar) {
        background-color: $primary;
      }
    }

    .tab-card {
      margin-top: 16px;
      padding: 28px 32px;

      :deep(.el-form-item__label) {
        color: $text-secondary;
        font-size: 13px;
      }

      :deep(.el-input__wrapper),
      :deep(.el-textarea__inner) {
        background: $bg-input;
        border-color: $border;

        &:focus-within {
          border-color: $primary;
        }
      }
    }

    .security-card {
      .security-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 0;

        .security-label {
          font-size: 14px;
          font-weight: 500;
          color: $text-primary;
          margin-bottom: 4px;
        }

        .security-value {
          font-size: 12px;
          color: $text-secondary;
        }
      }

      .danger-zone {
        margin-top: 20px;
        padding: 16px;
        background: rgba(239, 68, 68, 0.05);
        border: 1px solid rgba(239, 68, 68, 0.2);
        border-radius: $radius-md;

        .danger-title {
          font-size: 13px;
          font-weight: 600;
          color: #ef4444;
          margin-bottom: 12px;
        }
      }
    }
  }
}
</style>
