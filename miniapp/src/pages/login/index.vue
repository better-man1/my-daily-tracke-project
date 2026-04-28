<template>
  <view class="login-page">
    <!-- 顶部装饰 -->
    <view class="login-bg-orb orb1" />
    <view class="login-bg-orb orb2" />

    <view class="login-content">
      <!-- 品牌 -->
      <view class="brand">
        <text class="brand-icon">📅</text>
        <text class="brand-name">DailyTracker</text>
      </view>

      <view class="login-tagline">
        <text class="title">{{ isRegister ? '创建账号' : '欢迎回来' }}</text>
        <text class="subtitle">{{ isRegister ? '开启你的成长之旅' : '记录每一天，成就更好的自己' }}</text>
      </view>

      <!-- 表单 -->
      <view class="login-form">
        <view class="form-item">
          <text class="form-label">用户名</text>
          <input
            v-model="form.username"
            class="form-input"
            placeholder="请输入用户名"
            placeholder-style="color:#475569"
          />
        </view>

        <view v-if="isRegister" class="form-item">
          <text class="form-label">昵称（可选）</text>
          <input
            v-model="form.nickname"
            class="form-input"
            placeholder="请输入昵称"
            placeholder-style="color:#475569"
          />
        </view>

        <view class="form-item">
          <text class="form-label">密码</text>
          <input
            v-model="form.password"
            class="form-input"
            placeholder="请输入密码（至少6位）"
            placeholder-style="color:#475569"
            password
          />
        </view>

        <button class="submit-btn" :loading="loading" @tap="handleSubmit">
          {{ isRegister ? '注册' : '立即登录' }}
        </button>

        <view class="switch-link" @tap="isRegister = !isRegister">
          <text class="switch-text">
            {{ isRegister ? '已有账号？' : '还没有账号？' }}
          </text>
          <text class="switch-action">{{ isRegister ? '立即登录' : '立即注册' }}</text>
        </view>
      </view>

      <!-- 功能介绍 -->
      <view v-if="!isRegister" class="features-bar">
        <view v-for="item in features" :key="item.label" class="feature-item">
          <text class="feature-icon">{{ item.icon }}</text>
          <text class="feature-label">{{ item.label }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { authApi } from '../../api/index'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const isRegister = ref(false)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: ''
})

const features = [
  { icon: '📋', label: '计划' },
  { icon: '💰', label: '记账' },
  { icon: '📖', label: '摘录' },
  { icon: '🎯', label: '目标' },
  { icon: '✍️', label: '总结' }
]

async function handleSubmit() {
  if (!form.username.trim() || !form.password.trim()) {
    uni.showToast({ title: '请填写用户名和密码', icon: 'none' })
    return
  }
  if (form.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }

  loading.value = true
  try {
    if (isRegister.value) {
      await authApi.register({
        username: form.username,
        password: form.password,
        nickname: form.nickname || undefined
      })
      uni.showToast({ title: '注册成功，请登录', icon: 'success' })
      isRegister.value = false
    } else {
      const data = await authApi.login({ username: form.username, password: form.password })
      userStore.setLoginData(data)
      uni.showToast({ title: `欢迎，${data.nickname || data.username}！`, icon: 'success' })
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/index/index' })
      }, 800)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #0f1117;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 0 40rpx;
}

.login-bg-orb {
  position: fixed;
  border-radius: 50%;
  pointer-events: none;

  &.orb1 {
    width: 600rpx;
    height: 600rpx;
    background: radial-gradient(circle, rgba(99, 102, 241, 0.25), transparent);
    top: -200rpx;
    right: -200rpx;
  }

  &.orb2 {
    width: 400rpx;
    height: 400rpx;
    background: radial-gradient(circle, rgba(167, 139, 250, 0.15), transparent);
    bottom: -100rpx;
    left: -100rpx;
  }
}

.login-content {
  width: 100%;
  position: relative;
  z-index: 1;
}

.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  margin-bottom: 48rpx;

  .brand-icon { font-size: 60rpx; }
  .brand-name {
    font-size: 48rpx;
    font-weight: 800;
    background: linear-gradient(135deg, #818cf8, #a78bfa);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

.login-tagline {
  text-align: center;
  margin-bottom: 60rpx;

  .title {
    display: block;
    font-size: 52rpx;
    font-weight: 700;
    color: #e2e8f0;
    margin-bottom: 12rpx;
  }

  .subtitle {
    display: block;
    font-size: 28rpx;
    color: #94a3b8;
  }
}

.login-form {
  background: rgba(26, 29, 39, 0.9);
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 40rpx;
  padding: 48rpx;
}

.form-item {
  margin-bottom: 32rpx;

  .form-label {
    display: block;
    font-size: 26rpx;
    color: #94a3b8;
    margin-bottom: 12rpx;
  }

  .form-input {
    width: 100%;
    background: #252836;
    border: 1rpx solid rgba(255, 255, 255, 0.08);
    border-radius: 16rpx;
    padding: 24rpx 28rpx;
    font-size: 30rpx;
    color: #e2e8f0;
    box-sizing: border-box;
  }
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #6366f1, #7c3aed);
  color: white;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  margin-top: 16rpx;
}

.switch-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 32rpx;

  .switch-text { font-size: 26rpx; color: #94a3b8; }
  .switch-action { font-size: 26rpx; color: #818cf8; font-weight: 500; }
}

.features-bar {
  display: flex;
  justify-content: center;
  gap: 40rpx;
  margin-top: 48rpx;

  .feature-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;

    .feature-icon { font-size: 48rpx; }
    .feature-label { font-size: 22rpx; color: #64748b; }
  }
}
</style>
