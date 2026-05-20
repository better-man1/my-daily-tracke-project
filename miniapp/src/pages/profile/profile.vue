<!--
/**
 * ============================================================================
 * profile.vue — 个人中心（Tab 页）
 * ============================================================================
 *
 * 【页面说明】展示用户信息、使用统计、功能菜单、设置和退出登录
 * 【路由路径】/pages/profile/profile（Tab 页，uni.switchTab）
 * 【页面传参】无
 * 【关键 API】uni.navigateTo / uni.reLaunch / uni.showModal / onShow
 * ============================================================================
 */
-->

<template>
  <view class="page-container">
    <dt-navbar title="我的" />

    <view class="page-content">
      <!-- 用户信息卡片 -->
      <view class="profile-card fade-in">
        <view class="profile-avatar">
          <text class="avatar-text">{{ userStore.nickname?.charAt(0)?.toUpperCase() || 'U' }}</text>
        </view>
        <view class="profile-info">
          <text class="profile-name">{{ userStore.nickname }}</text>
          <text class="profile-username">@{{ userStore.userInfo.username }}</text>
        </view>
        <view class="profile-edit" @tap="goEdit">
          <text class="edit-icon">✏️</text>
        </view>
      </view>

      <!-- 使用统计 -->
      <view class="usage-stats slide-up">
        <view class="usage-item" @tap="goPage('goal')">
          <text class="usage-value">{{ statsData.goalCount || 0 }}</text>
          <text class="usage-label">个人目标</text>
        </view>
        <view class="usage-divider" />
        <view class="usage-item">
          <text class="usage-value">{{ statsData.planTotal || 0 }}</text>
          <text class="usage-label">累计计划</text>
        </view>
        <view class="usage-divider" />
        <view class="usage-item">
          <text class="usage-value">{{ statsData.excerptTotal || 0 }}</text>
          <text class="usage-label">摘录条数</text>
        </view>
        <view class="usage-divider" />
        <view class="usage-item" @tap="goPage('summary')">
          <text class="usage-value">{{ streakData.currentStreak || 0 }}</text>
          <text class="usage-label">连续打卡</text>
        </view>
      </view>

      <!-- 今日总结入口 -->
      <view class="summary-entry slide-up" @tap="goPage('summary')">
        <view class="summary-entry-left">
          <text class="summary-entry-icon">📔</text>
          <view class="summary-entry-text">
            <text class="summary-entry-title">今日总结</text>
            <text class="summary-entry-sub">{{ summaryStatus }}</text>
          </view>
        </view>
        <text class="summary-entry-arrow">›</text>
      </view>

      <!-- 功能菜单 -->
      <view class="menu-section slide-up">
        <text class="menu-section-title">功能</text>
        <view class="menu-list">
          <view v-for="item in menuItems" :key="item.id" class="menu-item" @tap="handleMenu(item)">
            <view class="menu-item-left">
              <view class="menu-item-icon" :style="{ background: item.iconBg }">
                <text class="menu-icon-text">{{ item.icon }}</text>
              </view>
              <text class="menu-item-label">{{ item.label }}</text>
            </view>
            <view class="menu-item-right">
              <text v-if="item.badge" class="menu-badge">{{ item.badge }}</text>
              <text class="menu-arrow">›</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 设置菜单 -->
      <view class="menu-section slide-up">
        <text class="menu-section-title">设置</text>
        <view class="menu-list">
          <view v-for="item in settingItems" :key="item.id" class="menu-item" @tap="handleMenu(item)">
            <view class="menu-item-left">
              <view class="menu-item-icon" :style="{ background: item.iconBg }">
                <text class="menu-icon-text">{{ item.icon }}</text>
              </view>
              <text class="menu-item-label">{{ item.label }}</text>
            </view>
            <view class="menu-item-right">
              <switch v-if="item.type === 'switch'" :checked="item.value" @change="handleSwitch(item, $event)" />
              <text v-else class="menu-arrow">›</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 退出登录 -->
      <view class="logout-btn slide-up" @tap="handleLogout">
        <text class="logout-text">退出登录</text>
      </view>

      <text class="version-text">DailyTracker v1.0.0</text>
    </view>
  </view>
</template>

<script setup lang="ts">
// Vue 3 Composition API
import { ref, computed } from 'vue'
// Uni-app 生命周期
import { onShow } from '@dcloudio/uni-app'
// Pinia 用户 Store
import { useUserStore } from '@/stores/user'
// 总结 API — 打卡/今日总结
import { summaryApi } from '@/api/summary'
// 目标 API — 统计
import { goalApi } from '@/api/goal'
// 摘录 API
import { excerptApi } from '@/api/excerpt'
// 计划 API
import { planApi } from '@/api/plan'


// ============================================================================
// 状态与数据
// ============================================================================
const userStore = useUserStore()

const statsData = ref<Record<string, number>>({})
const streakData = ref<{ currentStreak: number; longestStreak: number; totalDays: number }>({
  currentStreak: 0,
  longestStreak: 0,
  totalDays: 0
})
const hasTodaySummary = ref(false)

const summaryStatus = computed(() =>
  hasTodaySummary.value ? '✅ 今日已完成总结' : '📝 点击填写今日总结'
)

const menuItems = [
  { id: 'goal', icon: '🎯', label: '目标管理', iconBg: 'rgba(99, 102, 241, 0.1)' },
  { id: 'summary', icon: '📔', label: '每日总结', iconBg: 'rgba(16, 185, 129, 0.1)' },
  { id: 'summary-history', icon: '📊', label: '历史总结', iconBg: 'rgba(245, 158, 11, 0.1)' }
]

const settingItems = ref([
  { id: 'profile-edit', icon: '👤', label: '编辑资料', iconBg: 'rgba(99, 102, 241, 0.1)' },
  { id: 'password', icon: '🔒', label: '修改密码', iconBg: 'rgba(239, 68, 68, 0.1)' },
  { id: 'notification', icon: '🔔', label: '消息通知', iconBg: 'rgba(245, 158, 11, 0.1)', type: 'switch', value: false },
  { id: 'about', icon: 'ℹ️', label: '关于应用', iconBg: 'rgba(107, 114, 128, 0.1)' }
])


// ============================================================================
// 数据加载
// ============================================================================
async function loadData() {
  try {
    const [streak, todaySummary] = await Promise.all([
      summaryApi.getStreak().catch(() => ({ currentStreak: 0, longestStreak: 0, totalDays: 0 })),
      summaryApi.getToday().catch(() => null)
    ])
    streakData.value = streak
    hasTodaySummary.value = !!todaySummary

    // 异步加载其他统计（允许失败）
    goalApi.getStatistics().then(s => {
      statsData.value = { ...statsData.value, goalCount: s?.total || 0 }
    }).catch(() => {})
  } catch (e) {
    console.error('Profile load error:', e)
  }
}


// ============================================================================
// 页面导航
// ============================================================================
function goPage(page: string) {
  const map: Record<string, string> = {
    goal: '/sub-pages/goal/goal',
    summary: '/sub-pages/summary/summary'
  }
  if (map[page]) {
    uni.navigateTo({ url: map[page] })
  }
}

function goEdit() {
  uni.showToast({ title: '编辑资料功能开发中', icon: 'none' })
}

function handleMenu(item: any) {
  if (item.id === 'goal') goPage('goal')
  else if (item.id === 'summary' || item.id === 'summary-history') goPage('summary')
  else if (item.id === 'profile-edit') goEdit()
  else if (item.id === 'password') {
    uni.showToast({ title: '修改密码功能开发中', icon: 'none' })
  } else if (item.id === 'about') {
    uni.showModal({ title: 'DailyTracker', content: '版本: v1.0.0\n个人效率管理助手', showCancel: false })
  }
}

function handleSwitch(item: any, event: any) {
  item.value = event.detail.value
}

function handleLogout() {
  uni.showModal({
    title: '确认退出',
    content: '退出后需要重新登录，确定继续吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.reLaunch({ url: '/sub-pages/login/login' })
      }
    }
  })
}


// ============================================================================
// 生命周期
// ============================================================================
onShow(() => {
  if (userStore.isLoggedIn) {
    loadData()
  }
})
</script>

<style lang="scss" scoped>
// 用户卡片
.profile-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: linear-gradient(135deg, $dt-primary 0%, #818CF8 100%);
  border-radius: $dt-radius-xl;
  padding: 36rpx 32rpx;
  margin-bottom: $dt-space-lg;
  box-shadow: 0 8rpx 32rpx rgba(99, 102, 241, 0.3);
}

.profile-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 3rpx solid rgba(255, 255, 255, 0.4);

  .avatar-text {
    color: #FFFFFF;
    font-size: 40rpx;
    font-weight: 700;
  }
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.profile-name {
  font-size: $dt-font-xl;
  font-weight: 700;
  color: #FFFFFF;
}

.profile-username {
  font-size: $dt-font-sm;
  color: rgba(255, 255, 255, 0.7);
}

.profile-edit {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;

  .edit-icon { font-size: 32rpx; }
}

// 统计数据
.usage-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 32rpx 16rpx;
  margin-bottom: $dt-space-lg;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.usage-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.usage-value {
  font-size: $dt-font-2xl;
  font-weight: 700;
  color: $dt-primary;
}

.usage-label {
  font-size: $dt-font-xs;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.usage-divider {
  width: 1rpx;
  height: 64rpx;
  background: $dt-divider;
  @media (prefers-color-scheme: dark) { background: $dt-dark-border; }
}

// 今日总结入口
.summary-entry {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 28rpx 32rpx;
  margin-bottom: $dt-space-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &-left {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }

  &-icon { font-size: 48rpx; }

  &-text {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
  }

  &-title {
    font-size: $dt-font-md;
    font-weight: 600;
    color: $dt-text-primary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
  }

  &-sub {
    font-size: $dt-font-sm;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &-arrow {
    font-size: 40rpx;
    color: $dt-text-secondary;
    font-weight: 300;
  }
}

// 菜单
.menu-section {
  margin-bottom: $dt-space-lg;
}

.menu-section-title {
  font-size: $dt-font-sm;
  color: $dt-text-secondary;
  font-weight: 500;
  display: block;
  margin-bottom: 12rpx;
  padding-left: 4rpx;

  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.menu-list {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  overflow: hidden;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;

  & + .menu-item {
    border-top: 1rpx solid $dt-divider;
    @media (prefers-color-scheme: dark) { border-top-color: $dt-dark-border; }
  }

  &:active {
    background: $dt-bg-hover;
    @media (prefers-color-scheme: dark) { background: rgba(255, 255, 255, 0.03); }
  }

  &-left {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }

  &-icon {
    width: 72rpx;
    height: 72rpx;
    border-radius: $dt-radius-sm;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &-label {
    font-size: $dt-font-md;
    color: $dt-text-primary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
  }

  &-right {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }
}

.menu-icon-text { font-size: 36rpx; }

.menu-badge {
  font-size: $dt-font-xs;
  color: #FFFFFF;
  background: $dt-danger;
  padding: 2rpx 12rpx;
  border-radius: $dt-radius-full;
}

.menu-arrow {
  font-size: 40rpx;
  color: $dt-text-placeholder;
  font-weight: 300;
}

// 退出登录
.logout-btn {
  height: 88rpx;
  background: rgba(239, 68, 68, 0.08);
  border-radius: $dt-radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $dt-space-lg;

  &:active { opacity: 0.7; }

  .logout-text {
    font-size: $dt-font-md;
    color: $dt-danger;
    font-weight: 600;
  }
}

.version-text {
  display: block;
  text-align: center;
  font-size: $dt-font-xs;
  color: $dt-text-placeholder;
  padding-bottom: 48rpx;
}
</style>
