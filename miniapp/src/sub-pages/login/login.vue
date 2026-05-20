<!--
/**
 * ============================================================================
 * login.vue — 登录/注册页面
 * ============================================================================
 *
 * 【页面说明】用户登录和注册，支持模式切换、密码显隐
 * 【路由路径】/sub-pages/login/login
 * 【页面传参】无
 * 【关键 API】uni.switchTab / uni.showToast / uni.setStorageSync
 * ============================================================================
 */
-->

<template>
  <view class="login-page">
    <!-- 背景装饰 -->
    <view class="bg-blob bg-blob-1" />
    <view class="bg-blob bg-blob-2" />

    <!-- Logo 区域 -->
    <view class="logo-section">
      <view class="logo-icon">
        <text class="logo-emoji">📊</text>
      </view>
      <text class="logo-title">DailyTracker</text>
      <text class="logo-sub">记录每一天的美好与成长</text>
    </view>

    <!-- 登录表单 -->
    <view class="form-card fade-in">
      <!-- 标签切换 -->
      <view class="form-tabs">
        <view class="form-tab"
          :class="{ 'form-tab--active': mode === 'login' }"
          @tap="mode = 'login'">
          <text class="tab-text">登录</text>
        </view>
        <view class="form-tab"
          :class="{ 'form-tab--active': mode === 'register' }"
          @tap="mode = 'register'">
          <text class="tab-text">注册</text>
        </view>
        <view class="tab-indicator" :class="{ 'tab-indicator--right': mode === 'register' }" />
      </view>

      <!-- 表单内容 -->
      <view class="form-body">
        <view class="form-field">
          <view class="field-icon"><text>👤</text></view>
          <input
            class="field-input"
            v-model="form.username"
            type="text"
            placeholder="请输入用户名"
            placeholder-class="field-placeholder"
            maxlength="20"
          />
        </view>

        <view v-if="mode === 'register'" class="form-field">
          <view class="field-icon"><text>😊</text></view>
          <input
            class="field-input"
            v-model="form.nickname"
            type="text"
            placeholder="请输入昵称（可选）"
            placeholder-class="field-placeholder"
            maxlength="20"
          />
        </view>

        <view class="form-field">
          <view class="field-icon"><text>🔒</text></view>
          <input
            class="field-input"
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            placeholder-class="field-placeholder"
            maxlength="32"
          />
          <view class="field-eye" @tap="showPassword = !showPassword">
            <text class="eye-icon">{{ showPassword ? '👁' : '🙈' }}</text>
          </view>
        </view>

        <!-- 提交按钮 -->
        <button
          class="submit-btn"
          :loading="loading"
          :disabled="loading"
          @tap="handleSubmit">
          <text class="submit-text">{{ mode === 'login' ? '登 录' : '注 册' }}</text>
        </button>

        <!-- 底部提示 -->
        <text class="form-hint">
          {{ mode === 'login' ? '还没有账号？' : '已有账号？' }}
          <text class="hint-link" @tap="mode = mode === 'login' ? 'register' : 'login'">
            {{ mode === 'login' ? '立即注册' : '马上登录' }}
          </text>
        </text>
      </view>
    </view>

    <!-- 底部装饰 -->
    <view class="footer-text">
      <text class="footer-hint">登录即代表同意相关服务协议</text>
    </view>
  </view>
</template>

<script setup lang="ts">
// Vue 3 Composition API
import { ref } from 'vue'
// Pinia 用户 Store
import { useUserStore } from '@/stores/user'
// 认证 API — 登录/注册
import { authApi } from '@/api/auth'


// ============================================================================
// 状态与数据
// ============================================================================
const userStore = useUserStore()
const mode = ref<'login' | 'register'>('login')
const loading = ref(false)
const showPassword = ref(false)

const form = ref({
  username: '',
  nickname: '',
  password: ''
})


// ============================================================================
// 表单提交
// ============================================================================
async function handleSubmit() {
  const { username, password, nickname } = form.value

  if (!username.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }
  if (!password.trim()) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  if (password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }

  loading.value = true
  try {
    if (mode.value === 'login') {
      const res = await authApi.login({ username, password })
      userStore.setLoginData(res)
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/index/index' })
      }, 800)
    } else {
      await authApi.register({ username, password, nickname: nickname || undefined })
      uni.showToast({ title: '注册成功，请登录', icon: 'success' })
      setTimeout(() => {
        mode.value = 'login'
        form.value.password = ''
      }, 800)
    }
  } catch (err: any) {
    console.error('Auth error:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #EEF2FF 0%, #F0FDF4 50%, #FFF7ED 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 40rpx;
  overflow: hidden;
  position: relative;

  @media (prefers-color-scheme: dark) {
    background: linear-gradient(160deg, #1E1B4B 0%, #0F2318 50%, #1C1308 100%);
  }
}

// 背景装饰球
.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80rpx);
  opacity: 0.3;
  pointer-events: none;
}

.bg-blob-1 {
  width: 400rpx;
  height: 400rpx;
  background: $dt-primary;
  top: -100rpx;
  right: -80rpx;
}

.bg-blob-2 {
  width: 300rpx;
  height: 300rpx;
  background: $dt-success;
  bottom: 100rpx;
  left: -60rpx;
}

// Logo
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 160rpx;
  margin-bottom: 64rpx;
}

.logo-icon {
  width: 128rpx;
  height: 128rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, $dt-primary, $dt-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 40rpx rgba(99, 102, 241, 0.35);

  .logo-emoji { font-size: 64rpx; }
}

.logo-title {
  font-size: $dt-font-2xl;
  font-weight: 800;
  color: $dt-text-primary;
  letter-spacing: 2rpx;
  margin-bottom: 8rpx;

  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.logo-sub {
  font-size: $dt-font-sm;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

// 表单卡片
.form-card {
  width: 100%;
  background: rgba(255, 255, 255, 0.9);
  border-radius: $dt-radius-xl;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.08);
  overflow: hidden;

  @media (prefers-color-scheme: dark) {
    background: rgba(30, 41, 59, 0.9);
    box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.3);
  }
}

// 标签切换
.form-tabs {
  display: flex;
  align-items: center;
  position: relative;
  padding: 8rpx;
  background: rgba(0, 0, 0, 0.03);

  @media (prefers-color-scheme: dark) { background: rgba(255, 255, 255, 0.05); }
}

.form-tab {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;

  .tab-text {
    font-size: $dt-font-md;
    color: $dt-text-secondary;
    font-weight: 500;
    transition: color 0.25s;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &--active .tab-text {
    color: $dt-primary;
    font-weight: 700;
  }
}

.tab-indicator {
  position: absolute;
  left: 8rpx;
  top: 8rpx;
  width: calc(50% - 8rpx);
  height: 72rpx;
  background: #FFFFFF;
  border-radius: $dt-radius-md;
  box-shadow: $dt-shadow-sm;
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &--right {
    transform: translateX(calc(100% + 8rpx));
  }
}

// 表单内容
.form-body {
  padding: 40rpx 32rpx 48rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.form-field {
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.03);
  border-radius: $dt-radius-md;
  padding: 0 24rpx;
  height: 96rpx;
  gap: 16rpx;
  transition: all 0.2s;
  border: 2rpx solid transparent;

  @media (prefers-color-scheme: dark) { background: rgba(255, 255, 255, 0.06); }

  &:focus-within {
    border-color: $dt-primary;
    background: rgba(99, 102, 241, 0.04);
  }
}

.field-icon {
  font-size: 32rpx;
  flex-shrink: 0;
}

.field-input {
  flex: 1;
  height: 100%;
  font-size: $dt-font-md;
  color: $dt-text-primary;
  background: transparent;

  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.field-placeholder {
  color: $dt-text-placeholder;
  font-size: $dt-font-md;
}

.field-eye {
  flex-shrink: 0;
  padding: 8rpx;

  .eye-icon { font-size: 32rpx; }
}

// 提交按钮
.submit-btn {
  height: 96rpx;
  background: linear-gradient(135deg, $dt-primary, $dt-primary-light);
  border-radius: $dt-radius-md;
  border: none;
  margin-top: 8rpx;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.35);
  transition: all 0.2s;

  &::after { border: none; }

  &:active {
    opacity: 0.85;
    transform: scale(0.98);
  }

  .submit-text {
    color: #FFFFFF;
    font-size: $dt-font-lg;
    font-weight: 700;
    letter-spacing: 4rpx;
  }
}

// 底部提示
.form-hint {
  text-align: center;
  font-size: $dt-font-sm;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }

  .hint-link {
    color: $dt-primary;
    font-weight: 600;
  }
}

.footer-text {
  margin-top: 48rpx;
  .footer-hint {
    font-size: $dt-font-xs;
    color: $dt-text-placeholder;
  }
}
</style>
