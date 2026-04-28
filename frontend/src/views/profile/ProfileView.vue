<template>
  <div class="profile-view">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
    </div>
    <div class="card" style="max-width: 480px">
      <div class="profile-avatar">
        <div class="avatar-circle">{{ (userStore.nickname || 'U')[0].toUpperCase() }}</div>
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
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑个人资料" width="400px">
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
    <el-dialog v-model="showPwdDialog" title="修改登录密码" width="400px">
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

const userStore = useUserStore()
const router = useRouter()
const saving = ref(false)
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

async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
  userStore.logout()
  router.push('/login')
}

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
    }

    .profile-name {
      font-size: 18px;
      font-weight: 600;
    }
  }
}
</style>
