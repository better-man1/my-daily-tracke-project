<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-orb orb-1" />
    <div class="bg-orb orb-2" />
    <div class="bg-orb orb-3" />

    <div class="login-container animate-fade-in-up">
      <!-- 头部 -->
      <div class="login-header">
        <div class="brand">
          <span class="brand-icon">📅</span>
          <span class="brand-name gradient-text">DailyTracker</span>
        </div>
        <h1 class="login-title">欢迎回来</h1>
        <p class="login-subtitle">记录每一天，成就更好的自己</p>
      </div>

      <!-- 表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            id="login-username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            id="login-password"
            placeholder="密码"
            type="password"
            size="large"
            :prefix-icon="Lock"
            show-password
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button
          id="login-submit"
          class="login-btn"
          type="primary"
          size="large"
          :loading="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '立即登录' }}
        </el-button>
      </el-form>

      <!-- 注册链接 -->
      <p class="register-link">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </p>

      <!-- 功能亮点 -->
      <div class="features">
        <div class="feature-item" v-for="item in features" :key="item.label">
          <span class="feature-icon">{{ item.icon }}</span>
          <span class="feature-label">{{ item.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' }
  ]
}

const features = [
  { icon: '📋', label: '每日计划' },
  { icon: '💰', label: '智能记账' },
  { icon: '📖', label: '知识摘录' },
  { icon: '🎯', label: '目标追踪' },
  { icon: '✍️', label: '每日总结' }
]

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const data = await authApi.login(form)
    userStore.setLoginData(data)
    ElMessage.success(`欢迎回来，${data.nickname || data.username}！`)
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-page;
  position: relative;
  overflow: hidden;
}

// 背景光晕
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;

  &.orb-1 {
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(99, 102, 241, 0.3), transparent);
    top: -100px;
    right: -100px;
  }

  &.orb-2 {
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(167, 139, 250, 0.2), transparent);
    bottom: -100px;
    left: -50px;
  }

  &.orb-3 {
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(96, 165, 250, 0.15), transparent);
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
}

.login-container {
  width: 420px;
  background: rgba(26, 29, 39, 0.8);
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
  margin-bottom: 32px;

  .brand {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 20px;

    .brand-icon {
      font-size: 28px;
    }

    .brand-name {
      font-size: 22px;
      font-weight: 800;
      letter-spacing: -0.5px;
    }
  }

  .login-title {
    font-size: 24px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 6px;
  }

  .login-subtitle {
    font-size: 14px;
    color: $text-secondary;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 16px;
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
  letter-spacing: 0.5px;
  border-radius: $radius-md;
  margin-top: 8px;
  background: linear-gradient(135deg, $primary, #7c3aed) !important;
  border: none !important;
  transition: $transition-normal;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 8px 24px rgba($primary, 0.4) !important;
  }

  &:active {
    transform: translateY(0);
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

    &:hover {
      color: $primary;
    }
  }
}

.features {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid $border;

  .feature-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;

    .feature-icon {
      font-size: 18px;
    }
    .feature-label {
      font-size: 11px;
      color: $text-muted;
      white-space: nowrap;
    }
  }
}
</style>
