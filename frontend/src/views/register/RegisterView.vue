<template>
  <div class="login-page">
    <div class="bg-orb orb-1" />
    <div class="bg-orb orb-2" />

    <div class="login-container animate-fade-in-up">
      <div class="login-header">
        <div class="brand">
          <span class="brand-icon">📅</span>
          <span class="brand-name gradient-text">DailyTracker</span>
        </div>
        <h1 class="login-title">创建账号</h1>
        <p class="login-subtitle">开启你的成长之旅</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            id="register-username"
            placeholder="用户名（4~20位字母数字）"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            id="register-nickname"
            placeholder="昵称（可选）"
            size="large"
            :prefix-icon="Avatar"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            id="register-password"
            placeholder="密码（至少6位）"
            type="password"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            id="register-confirm-password"
            placeholder="确认密码"
            type="password"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-button
          id="register-submit"
          class="login-btn"
          type="primary"
          size="large"
          :loading="loading"
          @click="handleRegister"
        >
          {{ loading ? '注册中...' : '立即注册' }}
        </el-button>
      </el-form>

      <p class="register-link">
        已有账号？
        <router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Avatar } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (_rule: any, value: string, callback: Function) => {
  if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{4,20}$/, message: '用户名为4~20位字母数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleRegister() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await authApi.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || undefined
    })
    ElMessage.success('注册成功！请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-page;
  position: relative;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;

  &.orb-1 {
    width: 350px;
    height: 350px;
    background: radial-gradient(circle, rgba(99, 102, 241, 0.25), transparent);
    top: -80px;
    right: -80px;
  }
  &.orb-2 {
    width: 250px;
    height: 250px;
    background: radial-gradient(circle, rgba(167, 139, 250, 0.18), transparent);
    bottom: -80px;
    left: -40px;
  }
}

.login-container {
  width: 420px;
  background: rgba(26, 29, 39, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid $border;
  border-radius: $radius-xl;
  padding: 40px;
  position: relative;
  z-index: 1;
  box-shadow: $shadow-lg;
}

.login-header {
  text-align: center;
  margin-bottom: 28px;

  .brand {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 18px;
    .brand-icon {
      font-size: 28px;
    }
    .brand-name {
      font-size: 22px;
      font-weight: 800;
    }
  }

  .login-title {
    font-size: 22px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 4px;
  }

  .login-subtitle {
    font-size: 13px;
    color: $text-secondary;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 14px;
  }
  :deep(.el-input__wrapper) {
    padding: 4px 16px;
    border-radius: $radius-md;
  }
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  border-radius: $radius-md;
  margin-top: 8px;
  background: linear-gradient(135deg, $primary, #7c3aed) !important;
  border: none !important;
  transition: $transition-normal;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 8px 24px rgba($primary, 0.4) !important;
  }
}

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: $text-secondary;

  a {
    color: $primary-light;
    font-weight: 500;
  }
}
</style>
