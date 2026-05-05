<template>
  <scroll-view scroll-y class="page-container" refresher-enabled @refresherrefresh="loadData" :refresher-triggered="refreshing">
    <!-- 用户信息卡 -->
    <view class="user-hero">
      <view class="user-avatar-wrap">
        <view class="user-avatar">
          <text class="avatar-text">{{ userStore.nickname?.[0] ?? '?' }}</text>
        </view>
        <!-- 打卡连续天数徽章 -->
        <view v-if="streak.currentStreak" class="streak-badge">
          <text>🔥{{ streak.currentStreak }}</text>
        </view>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userStore.nickname }}</text>
        <text class="user-username">@{{ userStore.userInfo?.username }}</text>
        <text v-if="userStore.userInfo?.signature" class="user-signature">{{ userStore.userInfo.signature }}</text>
      </view>
    </view>

    <!-- 综合统计 -->
    <view class="stats-row">
      <view class="stat-item">
        <text class="stat-num">{{ streak.currentStreak ?? 0 }}</text>
        <text class="stat-label">连续打卡</text>
      </view>
      <view class="stat-divider" />
      <view class="stat-item">
        <text class="stat-num">{{ streak.longestStreak ?? 0 }}</text>
        <text class="stat-label">最长连续</text>
      </view>
      <view class="stat-divider" />
      <view class="stat-item">
        <text class="stat-num">{{ streak.totalDays ?? 0 }}</text>
        <text class="stat-label">总结天数</text>
      </view>
    </view>

    <!-- 本月概况 -->
    <view class="section-title">本月概况</view>
    <view class="month-overview">
      <view class="overview-card" style="--card-color:#6366f1">
        <text class="ov-icon">📋</text>
        <text class="ov-value">{{ monthData.planDone ?? 0 }}</text>
        <text class="ov-label">完成任务</text>
      </view>
      <view class="overview-card" style="--card-color:#10b981">
        <text class="ov-icon">💰</text>
        <text class="ov-value">¥{{ fmtAmt(monthData.expense) }}</text>
        <text class="ov-label">本月支出</text>
      </view>
      <view class="overview-card" style="--card-color:#f59e0b">
        <text class="ov-icon">📖</text>
        <text class="ov-value">{{ monthData.excerpts ?? 0 }}</text>
        <text class="ov-label">摘录数量</text>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="section-title">更多功能</view>
    <view class="menu-list">
      <view class="menu-section">
        <view class="menu-item" @tap="() => uni.navigateTo({ url: '/pages/goal/index' })">
          <view class="menu-icon-wrap" style="background:rgba(59,130,246,0.15)">
            <text class="menu-icon">🎯</text>
          </view>
          <text class="menu-label">目标管理</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @tap="() => uni.navigateTo({ url: '/pages/excerpt/index' })">
          <view class="menu-icon-wrap" style="background:rgba(245,158,11,0.15)">
            <text class="menu-icon">📖</text>
          </view>
          <text class="menu-label">摘录收藏</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="menu-section">
        <view class="menu-item" @tap="handleLogout">
          <view class="menu-icon-wrap" style="background:rgba(239,68,68,0.1)">
            <text class="menu-icon">🚪</text>
          </view>
          <text class="menu-label menu-danger">退出登录</text>
        </view>
      </view>
    </view>

    <!-- 版本信息 -->
    <view class="version-info">
      <text class="version-text">DailyTracker v1.0.0</text>
      <text class="version-desc">记录每一天，成就更好的自己 ✨</text>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { summaryApi, accountingApi, dashboardApi } from '../../api/index'

const userStore = useUserStore()
const refreshing = ref(false)
const streak = ref<any>({})
const monthData = ref<any>({})

const now = new Date()

function fmtAmt(v: any) { return Number(v ?? 0).toFixed(0) }

async function loadData() {
  try {
    // 打卡连续统计
    streak.value = await summaryApi.getStreak()

    // 本月记账概况
    const stats = await accountingApi.monthlyStats(now.getFullYear(), now.getMonth() + 1)
    monthData.value = { ...monthData.value, expense: stats.totalExpense, income: stats.totalIncome }

    // 今日总览（含任务完成数等）
    try {
      const today = await dashboardApi.getToday()
      monthData.value = {
        ...monthData.value,
        planDone: today.plan?.done,
        excerpts: today.excerptCount
      }
    } catch {}
  } catch (e) {
    console.error(e)
  } finally {
    refreshing.value = false
  }
}

async function handleLogout() {
  const result = await uni.showModal({
    title: '提示', content: '确认退出登录？',
    confirmText: '退出', confirmColor: '#ef4444'
  })
  if (result.confirm) {
    userStore.logout()
    uni.reLaunch({ url: '/pages/login/index' })
  }
}

onMounted(loadData)
onShow(loadData)
</script>

<style lang="scss" scoped>
.page-container {
  background: #0f1117;
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.user-hero {
  display: flex;
  align-items: center;
  gap: 28rpx;
  padding: 48rpx 32rpx 32rpx;

  .user-avatar-wrap {
    position: relative;
    flex-shrink: 0;

    .user-avatar {
      width: 128rpx;
      height: 128rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, #6366f1, #7c3aed);
      display: flex;
      align-items: center;
      justify-content: center;

      .avatar-text { font-size: 56rpx; font-weight: 700; color: white; }
    }

    .streak-badge {
      position: absolute;
      bottom: -6rpx;
      right: -6rpx;
      background: #1a1d27;
      border: 2rpx solid rgba(255, 255, 255, 0.1);
      border-radius: 100rpx;
      padding: 4rpx 12rpx;
      font-size: 22rpx;
      color: #f59e0b;
    }
  }

  .user-info {
    flex: 1;
    min-width: 0;

    .user-name { display: block; font-size: 40rpx; font-weight: 700; color: #e2e8f0; }
    .user-username { display: block; font-size: 26rpx; color: #94a3b8; margin-top: 6rpx; }
    .user-signature { display: block; font-size: 24rpx; color: #64748b; margin-top: 8rpx; line-height: 1.4; }
  }
}

.stats-row {
  display: flex;
  align-items: center;
  margin: 0 24rpx;
  background: #1a1d27;
  border-radius: 24rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  padding: 28rpx 0;

  .stat-item {
    flex: 1;
    text-align: center;

    .stat-num { display: block; font-size: 44rpx; font-weight: 700; color: #e2e8f0; line-height: 1; }
    .stat-label { display: block; font-size: 22rpx; color: #64748b; margin-top: 8rpx; }
  }

  .stat-divider {
    width: 1rpx;
    height: 60rpx;
    background: rgba(255, 255, 255, 0.08);
  }
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #64748b;
  padding: 32rpx 32rpx 16rpx;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.month-overview {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12rpx;
  padding: 0 24rpx;

  .overview-card {
    background: #1a1d27;
    border-radius: 20rpx;
    padding: 28rpx 16rpx;
    text-align: center;
    border: 1rpx solid rgba(255, 255, 255, 0.08);
    position: relative;
    overflow: hidden;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 3rpx;
      background: var(--card-color, #6366f1);
    }

    .ov-icon { display: block; font-size: 40rpx; margin-bottom: 12rpx; }
    .ov-value { display: block; font-size: 32rpx; font-weight: 700; color: #e2e8f0; line-height: 1; }
    .ov-label { display: block; font-size: 22rpx; color: #94a3b8; margin-top: 8rpx; }
  }
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 0 24rpx;
  margin-bottom: 40rpx;

  .menu-section {
    background: #1a1d27;
    border-radius: 24rpx;
    overflow: hidden;
    border: 1rpx solid rgba(255, 255, 255, 0.08);

    .menu-item {
      display: flex;
      align-items: center;
      gap: 20rpx;
      padding: 28rpx;

      &:not(:last-child) {
        border-bottom: 1rpx solid rgba(255, 255, 255, 0.06);
      }

      .menu-icon-wrap {
        width: 72rpx;
        height: 72rpx;
        border-radius: 18rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        .menu-icon { font-size: 36rpx; }
      }

      .menu-label {
        flex: 1;
        font-size: 30rpx;
        color: #e2e8f0;

        &.menu-danger { color: #ef4444; }
      }

      .menu-arrow {
        font-size: 40rpx;
        color: #475569;
      }
    }
  }
}

.version-info {
  text-align: center;
  padding: 40rpx;

  .version-text { display: block; font-size: 26rpx; color: #475569; margin-bottom: 8rpx; }
  .version-desc { display: block; font-size: 24rpx; color: #374151; }
}
</style>
