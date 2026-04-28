<template>
  <view class="page-container">
    <view class="page-header">
      <view>
        <text class="page-title">我的</text>
        <text class="page-subtitle">个人中心</text>
      </view>
    </view>

    <!-- 用户信息卡 -->
    <view class="user-card card">
      <view class="user-avatar">
        <text class="avatar-text">{{ userStore.nickname[0] }}</text>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userStore.nickname }}</text>
        <text class="user-username">@{{ userStore.userInfo.username }}</text>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-list">
      <view class="menu-section">
        <view class="menu-item" @tap="() => uni.navigateTo({ url: '/pages/goal/index' })">
          <text class="menu-icon">🎯</text>
          <text class="menu-label">目标管理</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @tap="() => uni.navigateTo({ url: '/pages/excerpt/index' })">
          <text class="menu-icon">📖</text>
          <text class="menu-label">每日摘录</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="menu-section">
        <view class="menu-item" @tap="handleLogout">
          <text class="menu-icon">🚪</text>
          <text class="menu-label menu-danger">退出登录</text>
        </view>
      </view>
    </view>

    <!-- 版本信息 -->
    <view class="version-info">
      <text class="version-text">DailyTracker v1.0.0</text>
      <text class="version-desc">记录每一天，成就更好的自己</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

async function handleLogout() {
  const result = await uni.showModal({ title: '提示', content: '确认退出登录？', confirmText: '退出', confirmColor: '#ef4444' })
  if (result.confirm) {
    userStore.logout()
    uni.reLaunch({ url: '/pages/login/index' })
  }
}
</script>

<style lang="scss" scoped>
.page-container { background: #0f1117; min-height: 100vh; padding: 24rpx; padding-bottom: 120rpx; }

.page-header { margin-bottom: 24rpx;
  .page-title { font-size: 44rpx; font-weight: 700; color: #e2e8f0; display: block; }
  .page-subtitle { font-size: 24rpx; color: #94a3b8; }
}

.user-card { display: flex; align-items: center; gap: 28rpx; margin-bottom: 32rpx;
  .user-avatar { width: 120rpx; height: 120rpx; border-radius: 50%; background: linear-gradient(135deg, #6366f1, #7c3aed); display: flex; align-items: center; justify-content: center; flex-shrink: 0;
    .avatar-text { font-size: 52rpx; font-weight: 700; color: white; }
  }
  .user-name { font-size: 36rpx; font-weight: 700; color: #e2e8f0; display: block; }
  .user-username { font-size: 26rpx; color: #94a3b8; margin-top: 8rpx; }
}

.menu-list { display: flex; flex-direction: column; gap: 16rpx; margin-bottom: 40rpx; }

.menu-section { background: #1a1d27; border-radius: 24rpx; overflow: hidden; border: 1rpx solid rgba(255,255,255,0.08); }

.menu-item { display: flex; align-items: center; gap: 20rpx; padding: 32rpx 28rpx;
  &:not(:last-child) { border-bottom: 1rpx solid rgba(255,255,255,0.06); }
  .menu-icon { font-size: 40rpx; }
  .menu-label { flex: 1; font-size: 30rpx; color: #e2e8f0; }
  .menu-danger { color: #ef4444; }
  .menu-arrow { font-size: 36rpx; color: #475569; }
}

.version-info { text-align: center; padding: 40rpx;
  .version-text { display: block; font-size: 26rpx; color: #475569; margin-bottom: 8rpx; }
  .version-desc { display: block; font-size: 24rpx; color: #374151; }
}
</style>
