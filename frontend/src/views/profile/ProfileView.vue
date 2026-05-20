<!--
/**
 * ============================================================================
 * ProfileView.vue — 个人中心页面组件
 * ============================================================================
 *
 * 【组件说明】
 * 用户个人资料管理页面。
 *
 * 【主要功能】
 * - 用户信息展示与编辑
 * - 头像上传
 * - 修改密码
 * - 使用统计数据
 * - 全量数据导出
 * - 退出登录
 * ============================================================================
 */
-->

<template>
  <div class="profile-view">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
    </div>
    <div class="card" style="max-width: 480px">
      <div class="profile-avatar">
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :http-request="handleAvatarUpload"
          :before-upload="beforeAvatarUpload"
        >
          <div v-if="userStore.avatar" class="avatar-image">
            <img :src="userStore.avatar" alt="avatar" />
            <div class="avatar-hover">
              <el-icon><Camera /></el-icon>
            </div>
          </div>
          <div v-else class="avatar-circle">
            {{ (userStore.nickname || 'U')[0].toUpperCase() }}
            <div class="avatar-hover">
              <el-icon><Camera /></el-icon>
            </div>
          </div>
        </el-upload>
        <div>
          <div class="profile-name">{{ userStore.nickname }}</div>
          <div class="profile-username text-muted text-sm">@{{ userStore.username }}</div>
        </div>
      </div>

      <div class="profile-details text-sm mt-md">
        <div class="detail-item mb-xs">
          <span class="text-muted">电子邮箱：</span>
          <span>{{ profileData.email || '未设置' }}</span>
        </div>
        <div class="detail-item mb-xs">
          <span class="text-muted">手机号码：</span>
          <span>{{ profileData.phone || '未设置' }}</span>
        </div>
        <div class="detail-item">
          <span class="text-muted">个人签名：</span>
          <span>{{ profileData.signature || '暂无签名' }}</span>
        </div>
      </div>

      <el-divider />

      <div class="flex flex-col gap-sm">
        <el-button type="primary" @click="showEditDialog = true">编辑个人资料</el-button>
        <el-button type="warning" plain @click="showPwdDialog = true">修改登录密码</el-button>
        <el-button type="success" plain @click="handleExport" :loading="exporting">导出全量数据 (JSON)</el-button>
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑个人资料" width="540px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="签名">
          <el-input v-model="editForm.signature" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleUpdateProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="showPwdDialog" title="修改登录密码" width="500px">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPwdDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleChangePassword">提交修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { Camera } from '@element-plus/icons-vue'

// ============================================================================
// // 状态
// ============================================================================

// ============================================================================
// // 状态
// ============================================================================

const userStore = useUserStore()
const router = useRouter()
const saving = ref(false)
const exporting = ref(false)
const showEditDialog = ref(false)
const showPwdDialog = ref(false)

const profileData = ref({
  email: '',
  phone: '',
  signature: ''
})

const editForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  signature: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '长度至少6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const pwdFormRef = ref<FormInstance>()

// ============================================================================
// // 数据加载
// ============================================================================

/**
 * 加载用户个人资料
 *
 * 功能说明：从后端获取用户的详细信息
 * 业务逻辑：
 * 1. 调用userApi.getProfile()获取用户数据
 * 2. 提取email、phone、signature字段
 * 3. 同时填充到profileData和editForm（编辑表单）
 *
 * 使用场景：组件初始化时调用
 *
 * @returns Promise<void>
 */
async function loadProfile() {
  const data = await userApi.getProfile()
  profileData.value = {
    email: data.email || '',
    phone: data.phone || '',
    signature: data.signature || ''
  }
  Object.assign(editForm, {
    nickname: data.nickname,
    email: data.email || '',
    phone: data.phone || '',
    signature: data.signature || ''
  })
}

// ============================================================================
// // 交互处理
// ============================================================================

// ============================================================================
// // 交互处理
// ============================================================================

async function handleUpdateProfile() {
  saving.value = true
  try {
    await userApi.updateProfile(editForm)
    ElMessage.success('个人资料已更新')
    userStore.updateUserInfo({ nickname: editForm.nickname })
    showEditDialog.value = false
    loadProfile()
  } finally {
    saving.value = false
  }
}

// ============================================================================
// // 导航
// ============================================================================

// ============================================================================
// // 导航
// ============================================================================

async function handleChangePassword() {
  await pwdFormRef.value?.validate()
  saving.value = true
  try {
    await userApi.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    showPwdDialog.value = false
    userStore.logout()
    router.push('/login')
  } finally {
    saving.value = false
  }
}

/**
 * 退出登录
 *
 * 功能说明：清除登录状态并跳转到登录页
 * 业务逻辑：
 * 1. 弹出确认对话框
 * 2. 调用userStore.logout()清除token和用户信息
 * 3. 跳转到登录页面
 *
 * 使用场景：用户点击"退出登录"按钮时调用
 */
async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', {
    type: 'warning',
    customClass: 'logout-confirm-box'
  })
  userStore.logout()
  router.push('/login')
}

/**
 * 头像上传前的校验
 *
 * 功能说明：校验上传文件的格式和大小
 * 业务逻辑：
 * 1. 检查文件类型：只允许JPG/PNG/WebP格式
 * 2. 检查文件大小：不能超过2MB
 * 3. 任一校验不通过则返回false阻止上传
 *
 * 使用场景：用户选择头像文件后自动调用
 *
 * @param file - 文件对象
 * @returns 是否通过校验
 */
const beforeAvatarUpload = (file: any) => {
  const isJPGorPNG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGorPNG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG/WebP 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isJPGorPNG && isLt2M
}

/**
 * 处理头像上传
 *
 * 功能说明：将用户选择的上传图片发送到后端并更新头像
 * 业务逻辑：
 * 1. 创建FormData对象并添加文件
 * 2. 调用userApi.updateAvatar()上传
 * 3. 成功后更新store中的avatar字段
 * 4. 显示成功或失败提示
 *
 * 使用场景：用户通过el-upload组件选择文件后自动调用
 *
 * @param options - 上传选项对象，包含file字段
 * @returns Promise<void>
 */
const handleAvatarUpload = async (options: any) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const avatarUrl = await userApi.updateAvatar(formData)
    userStore.updateUserInfo({ avatar: avatarUrl })
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('Avatar upload error:', error)
    ElMessage.error('头像上传失败')
  }
}

/**
 * 导出全量用户数据
 *
 * 功能说明：将用户的所有数据导出为JSON文件下载
 * 业务逻辑：
 * 1. 调用userApi.exportData()获取所有数据
 * 2. 将数据格式化为JSON字符串（缩进2个空格）
 * 3. 创建Blob对象（类型为application/json）
 * 4. 创建临时下载链接并触发点击
 * 5. 清理临时链接和URL对象
 * 6. 生成文件名格式：daily-tracker-data-YYYY-MM-DD.json
 *
 * 使用场景：用户点击"导出全量数据"按钮时调用
 *
 * @returns Promise<void>
 */
async function handleExport() {
  exporting.value = true
  try {
    const data = await userApi.exportData()
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `daily-tracker-data-${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('数据导出成功')
  } catch (error) {
    console.error('Export error:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// ============================================================================
// // 生命周期
// ============================================================================

// ============================================================================
// // 生命周期
// ============================================================================

onMounted(loadProfile)
</script>

<style lang="scss" scoped>
.profile-view {
  .profile-avatar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 8px;

    .avatar-circle {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: linear-gradient(135deg, $primary, #7c3aed);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 26px;
      font-weight: 700;
      color: white;
      position: relative;
      overflow: hidden;

      &:hover .avatar-hover {
        opacity: 1;
      }
    }

    .avatar-image {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      position: relative;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      &:hover .avatar-hover {
        opacity: 1;
      }
    }

    .avatar-hover {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 20px;
      opacity: 0;
      transition: opacity 0.2s ease;
    }

    .profile-name {
      font-size: 18px;
      font-weight: 600;
    }
  }
}
</style>
