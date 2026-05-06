<template>
  <scroll-view scroll-y class="page-container" refresher-enabled @refresherrefresh="loadData" :refresher-triggered="refreshing">
    <!-- 顶部用户问候 -->
    <view class="greeting-bar">
      <view class="greeting-left">
        <text class="greeting-text">{{ greeting }}，{{ userStore.nickname }}</text>
        <text class="greeting-date">{{ todayStr }}</text>
      </view>
      <view class="greeting-avatar" @tap="() => uni.switchTab({ url: '/pages/profile/index' })">
        <text class="avatar-text">{{ userStore.nickname[0] }}</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-grid">
      <view class="stat-card" style="--stat-color: #6366f1">
        <view class="stat-header">
          <text class="stat-icon">📋</text>
          <text class="stat-badge">计划</text>
        </view>
        <text class="stat-value">{{ planStats.done ?? 0 }}/{{ planStats.total ?? 0 }}</text>
        <view class="stat-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: (planStats.completionRate ?? 0) + '%', background: '#6366f1' }" />
          </view>
          <text class="stat-label">{{ planStats.completionRate ?? 0 }}% 完成</text>
        </view>
      </view>

      <view class="stat-card" style="--stat-color: #10b981">
        <view class="stat-header">
          <text class="stat-icon">💰</text>
          <text class="stat-badge">记账</text>
        </view>
        <text class="stat-value">¥{{ formatAmt(accounting.totalExpense) }}</text>
        <text class="stat-label">支出 | 收入 ¥{{ formatAmt(accounting.totalIncome) }}</text>
      </view>

      <view class="stat-card half" style="--stat-color: #f59e0b">
        <text class="stat-icon">📖</text>
        <text class="stat-num">{{ excerptCount }}</text>
        <text class="stat-label">今日摘录</text>
      </view>

      <view class="stat-card half" style="--stat-color: #ec4899">
        <text class="stat-icon">✍️</text>
        <text class="stat-num" :style="{ color: summaryInfo.hasSummary ? '#10b981' : '#64748b' }">
          {{ summaryInfo.hasSummary ? '✓' : '✗' }}
        </text>
        <text class="stat-label">今日总结</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="section-title">快速记录</view>
    <view class="quick-actions">
      <view v-for="action in quickActions" :key="action.label" class="action-item" @tap="action.fn">
        <view class="action-icon-wrap" :style="{ background: action.bg }">
          <text class="action-icon">{{ action.icon }}</text>
        </view>
        <text class="action-label">{{ action.label }}</text>
      </view>
    </view>

    <!-- 今日任务预览 -->
    <view class="section-title">
      今日任务
      <text class="section-more" @tap="() => uni.switchTab({ url: '/pages/plan/index' })">查看全部</text>
    </view>

    <view v-if="todayPlans.length" class="plan-preview">
      <view
        v-for="plan in todayPlans.slice(0, 4)"
        :key="plan.id"
        class="plan-item"
        :class="{ done: plan.status === 'DONE' }"
        @tap="togglePlan(plan)"
      >
        <view class="plan-check" :class="plan.status">
          <text v-if="plan.status === 'DONE'" class="check-icon">✓</text>
        </view>
        <view class="plan-info">
          <text class="plan-title">{{ plan.title }}</text>
          <text class="plan-priority" :class="`priority-${plan.priority.toLowerCase()}`">{{ plan.priority }}</text>
        </view>
      </view>
    </view>
    <view v-else class="empty-state">
      <text class="empty-icon">📋</text>
      <text class="empty-text">今天还没有任务</text>
    </view>

    <!-- 连续打卡 -->
    <view class="streak-bar card">
      <text class="streak-icon">🔥</text>
      <view class="streak-info">
        <text class="streak-num">{{ streak.currentStreak ?? 0 }}</text>
        <text class="streak-label">天连续总结打卡</text>
      </view>
      <button class="streak-btn" @tap="() => uni.switchTab({ url: '/pages/summary/index' })">
        {{ summaryInfo.hasSummary ? '已完成' : '去总结' }}
      </button>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { dashboardApi, planApi, summaryApi } from '../../api/index'

const userStore = useUserStore()
const refreshing = ref(false)

const planStats = ref<any>({})
const accounting = ref<any>({})
const excerptCount = ref(0)
const summaryInfo = ref<any>({})
const todayPlans = ref<any[]>([])
const streak = ref<any>({})

const todayStr = computed(() => {
  const d = new Date()
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getMonth() + 1}月${d.getDate()}日 周${days[d.getDay()]}`
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const quickActions = [
  { icon: '➕', label: '加计划', bg: 'rgba(99,102,241,0.15)', fn: () => uni.switchTab({ url: '/pages/plan/index' }) },
  { icon: '💸', label: '记一笔', bg: 'rgba(16,185,129,0.15)', fn: () => uni.switchTab({ url: '/pages/accounting/index' }) },
  { icon: '📝', label: '写摘录', bg: 'rgba(245,158,11,0.15)', fn: () => uni.navigateTo({ url: '/pages/excerpt/index' }) },
  { icon: '✍️', label: '写总结', bg: 'rgba(236,72,153,0.15)', fn: () => uni.switchTab({ url: '/pages/summary/index' }) }
]

function formatAmt(v: any) { return Number(v ?? 0).toFixed(2) }

async function loadData() {
  try {
    const today = await dashboardApi.getToday()
    planStats.value = today.plan ?? {}
    accounting.value = today.accounting ?? {}
    excerptCount.value = today.excerptCount ?? 0
    summaryInfo.value = today.summary ?? {}

    const plans = await planApi.list(new Date().toISOString().split('T')[0])
    todayPlans.value = plans || []

    const s = await summaryApi.getStreak()
    streak.value = s
  } catch (e) {
    console.error(e)
  } finally {
    refreshing.value = false
  }
}

async function togglePlan(plan: any) {
  const next = plan.status === 'DONE' ? 'TODO' : 'DONE'
  await planApi.updateStatus(plan.id, next)
  plan.status = next
}

onMounted(loadData)
onShow(loadData)
</script>

<style lang="scss" scoped>
.page-container { background: #0f1117; min-height: 100vh; padding-bottom: 120rpx; width: 100%; overflow-x: hidden; }

.greeting-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 40rpx 32rpx 24rpx;
  .greeting-text { display: block; font-size: 40rpx; font-weight: 700; color: #e2e8f0; }
  .greeting-date { font-size: 26rpx; color: #64748b; margin-top: 6rpx; }
  .greeting-avatar {
    width: 80rpx; height: 80rpx; border-radius: 50%;
    background: linear-gradient(135deg, #6366f1, #7c3aed);
    display: flex; align-items: center; justify-content: center;
    .avatar-text { font-size: 32rpx; font-weight: 700; color: white; }
  }
}

.stats-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 16rpx; padding: 0 24rpx 24rpx;
  .stat-card {
    background: #1a1d27; border-radius: 24rpx; padding: 28rpx;
    border: 1rpx solid rgba(255,255,255,0.08); position: relative; overflow: hidden;
    &::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4rpx; background: var(--stat-color, #6366f1); }
    &.half { display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; padding: 32rpx 20rpx; }
    .stat-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16rpx; }
    .stat-badge { font-size: 22rpx; color: #64748b; background: rgba(255,255,255,0.06); padding: 4rpx 12rpx; border-radius: 100rpx; }
    .stat-icon { font-size: 44rpx; }
    .stat-num { font-size: 64rpx; font-weight: 700; color: #e2e8f0; line-height: 1; margin: 12rpx 0; }
    .stat-value { font-size: 40rpx; font-weight: 700; color: #e2e8f0; display: block; margin-bottom: 12rpx; }
    .stat-label { font-size: 24rpx; color: #94a3b8; }
    .stat-progress { margin-top: 12rpx; }
    .progress-bar { height: 8rpx; background: rgba(255,255,255,0.08); border-radius: 4rpx; overflow: hidden; margin-bottom: 8rpx; }
    .progress-fill { height: 100%; border-radius: 4rpx; }
  }
}

.section-title {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8rpx 32rpx 20rpx; font-size: 30rpx; font-weight: 600; color: #94a3b8;
  .section-more { font-size: 26rpx; color: #6366f1; font-weight: 400; }
}

.quick-actions {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16rpx; padding: 0 24rpx 32rpx;
  .action-item { 
    display: flex; flex-direction: column; align-items: center; gap: 14rpx; 
    transition: transform 0.2s ease;
    &:active { transform: scale(0.92); }
  }
  .action-icon-wrap { 
    width: 108rpx; height: 108rpx; border-radius: 36rpx; display: flex; align-items: center; justify-content: center; 
    box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.15);
    background: linear-gradient(145deg, rgba(255,255,255,0.05), rgba(255,255,255,0));
  }
  .action-icon { font-size: 52rpx; }
  .action-label { font-size: 24rpx; color: #cbd5e1; font-weight: 500; }
}

.plan-preview {
  padding: 0 24rpx 16rpx; display: flex; flex-direction: column; gap: 12rpx;
  .plan-item {
    display: flex; align-items: center; gap: 20rpx;
    background: #1a1d27; border: 1rpx solid rgba(255,255,255,0.08);
    border-radius: 20rpx; padding: 24rpx;
    &.done { opacity: 0.5; .plan-title { text-decoration: line-through; } }
    .plan-check {
      width: 44rpx; height: 44rpx; border-radius: 50%;
      border: 2rpx solid #64748b; display: flex; align-items: center; justify-content: center; flex-shrink: 0;
      &.DONE { background: #10b981; border-color: #10b981; }
      &.IN_PROGRESS { border-color: #f59e0b; }
      .check-icon { color: white; font-size: 24rpx; }
    }
    .plan-info { flex: 1; display: flex; align-items: center; justify-content: space-between; }
    .plan-title { font-size: 28rpx; color: #e2e8f0; }
    .plan-priority { font-size: 22rpx; font-weight: 600; padding: 4rpx 12rpx; border-radius: 8rpx; background: rgba(255,255,255,0.06);
      &.priority-p0 { color: #ef4444; } &.priority-p1 { color: #f59e0b; } &.priority-p2 { color: #818cf8; } &.priority-p3 { color: #64748b; }
    }
  }
}

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 48rpx 24rpx;
  .empty-icon { font-size: 80rpx; margin-bottom: 16rpx; }
  .empty-text { font-size: 28rpx; color: #64748b; }
}

.streak-bar {
  margin: 16rpx 24rpx; display: flex; align-items: center; gap: 24rpx;
  .streak-icon { font-size: 48rpx; }
  .streak-info { flex: 1; }
  .streak-num { font-size: 48rpx; font-weight: 700; color: #f59e0b; display: block; line-height: 1; }
  .streak-label { font-size: 24rpx; color: #94a3b8; }
  .streak-btn { background: rgba(99,102,241,0.15); color: #818cf8; border: 1rpx solid rgba(99,102,241,0.3);
    border-radius: 16rpx; padding: 16rpx 32rpx; font-size: 26rpx; min-width: 0; line-height: 1.4; }
}
</style>
