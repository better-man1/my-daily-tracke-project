<template>
  <view class="page-container">
    <dt-navbar>
      <template #left>
        <view class="header-greeting">
          <text class="greeting-emoji">👋</text>
        </view>
      </template>
      <template #center>
        <view class="header-center">
          <text class="header-date">{{ todayStr }}</text>
          <text class="header-weekday">{{ weekdayStr }}</text>
        </view>
      </template>
      <template #right>
        <view class="header-avatar" @tap="goProfile">
          <text class="avatar-text">{{ userStore.nickname?.charAt(0) || 'U' }}</text>
        </view>
      </template>
    </dt-navbar>

    <view class="page-content">
      <!-- 欢迎横幅 -->
      <view class="welcome-banner fade-in">
        <view class="welcome-text">
          <text class="welcome-hi">{{ greetingText }}，{{ userStore.nickname }}</text>
          <text class="welcome-sub">今天也要元气满满哦 ✨</text>
        </view>
        <text class="welcome-emoji">🚀</text>
      </view>

      <!-- 今日概览数据 -->
      <view class="stats-grid slide-up">
        <view class="stat-card stat-card--plan" @tap="goTab('plan')">
          <view class="stat-card__icon-wrap">
            <text class="stat-card__icon">📋</text>
          </view>
          <view class="stat-card__info">
            <text class="stat-card__value">{{ todayData.planDone || 0 }}/{{ todayData.planTotal || 0 }}</text>
            <text class="stat-card__label">今日计划</text>
          </view>
          <view v-if="todayData.planTotal" class="stat-card__progress">
            <view class="stat-card__progress-bar"
              :style="{ width: (todayData.planDone / todayData.planTotal * 100) + '%' }" />
          </view>
        </view>

        <view class="stat-card stat-card--accounting" @tap="goTab('accounting')">
          <view class="stat-card__icon-wrap">
            <text class="stat-card__icon">💰</text>
          </view>
          <view class="stat-card__info">
            <text class="stat-card__value">¥{{ formatMoney(todayData.expense || 0) }}</text>
            <text class="stat-card__label">今日支出</text>
          </view>
        </view>

        <view class="stat-card stat-card--excerpt" @tap="goTab('excerpt')">
          <view class="stat-card__icon-wrap">
            <text class="stat-card__icon">📝</text>
          </view>
          <view class="stat-card__info">
            <text class="stat-card__value">{{ todayData.excerptCount || 0 }}</text>
            <text class="stat-card__label">今日摘录</text>
          </view>
        </view>

        <view class="stat-card stat-card--mood">
          <view class="stat-card__icon-wrap">
            <text class="stat-card__icon">{{ moodEmoji }}</text>
          </view>
          <view class="stat-card__info">
            <text class="stat-card__value">{{ todayData.mood || '--' }}</text>
            <text class="stat-card__label">今日心情</text>
          </view>
        </view>
      </view>

      <!-- 快捷操作 -->
      <dt-card title="快捷操作" icon="⚡">
        <view class="quick-actions">
          <view class="quick-action" @tap="navigateTo('/sub-pages/plan-add/plan-add')">
            <view class="quick-action__icon" style="background: rgba(99, 102, 241, 0.1);">
              <text>📋</text>
            </view>
            <text class="quick-action__text">新建计划</text>
          </view>
          <view class="quick-action" @tap="navigateTo('/sub-pages/accounting-add/accounting-add')">
            <view class="quick-action__icon" style="background: rgba(16, 185, 129, 0.1);">
              <text>💰</text>
            </view>
            <text class="quick-action__text">记一笔</text>
          </view>
          <view class="quick-action" @tap="navigateTo('/sub-pages/excerpt-add/excerpt-add')">
            <view class="quick-action__icon" style="background: rgba(245, 158, 11, 0.1);">
              <text>✍️</text>
            </view>
            <text class="quick-action__text">写摘录</text>
          </view>
          <view class="quick-action" @tap="navigateTo('/sub-pages/summary/summary')">
            <view class="quick-action__icon" style="background: rgba(239, 68, 68, 0.1);">
              <text>📔</text>
            </view>
            <text class="quick-action__text">日总结</text>
          </view>
        </view>
      </dt-card>

      <!-- 今日计划预览 -->
      <dt-card title="今日计划" icon="📋">
        <template #header-right>
          <text class="card-more" @tap="goTab('plan')">查看全部 ›</text>
        </template>

        <view v-if="todayPlans.length === 0" class="mini-empty">
          <text class="mini-empty__text">还没有今日计划，快去添加吧~</text>
        </view>

        <view v-for="item in todayPlans.slice(0, 5)" :key="item.id" class="plan-item">
          <view class="plan-item__check"
            :class="{ 'plan-item__check--done': item.status === 'DONE' }"
            @tap.stop="togglePlanStatus(item)">
            <text v-if="item.status === 'DONE'" class="plan-item__check-icon">✓</text>
          </view>
          <view class="plan-item__content">
            <text class="plan-item__title" :class="{ 'plan-item__title--done': item.status === 'DONE' }">
              {{ item.title }}
            </text>
            <view class="plan-item__meta">
              <text class="plan-item__tag" :class="'tag-' + item.priority.toLowerCase()">{{ item.priority }}</text>
              <text class="plan-item__category">{{ categoryLabel(item.category) }}</text>
            </view>
          </view>
        </view>
      </dt-card>

      <!-- 每日一句 -->
      <view v-if="randomExcerpt" class="quote-card slide-up">
        <text class="quote-mark">"</text>
        <text class="quote-content">{{ randomExcerpt.content }}</text>
        <text v-if="randomExcerpt.sourceTitle" class="quote-source">—— {{ randomExcerpt.sourceTitle }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { dashboardApi } from '@/api/dashboard'
import { planApi, type PlanItem } from '@/api/plan'
import { excerptApi, type ExcerptItem } from '@/api/excerpt'
import { formatDate, getWeekDay, formatMoney } from '@/utils/date'

const userStore = useUserStore()

// 今天的日期信息
const today = new Date()
const todayStr = formatDate(today)
const weekdayStr = getWeekDay(today)

// 问候语
const greetingText = computed(() => {
  const hour = today.getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 数据
const todayData = ref<Record<string, any>>({})
const todayPlans = ref<PlanItem[]>([])
const randomExcerpt = ref<ExcerptItem | null>(null)

const moodEmoji = computed(() => {
  const mood = todayData.value.mood
  if (!mood) return '😶'
  if (mood >= 8) return '😄'
  if (mood >= 6) return '🙂'
  if (mood >= 4) return '😐'
  return '😔'
})

const categoryLabel = (cat: string) => {
  const map: Record<string, string> = { WORK: '工作', STUDY: '学习', LIFE: '生活', HEALTH: '健康' }
  return map[cat] || cat
}

// 加载数据
async function loadData() {
  try {
    const [dashboard, plans] = await Promise.all([
      dashboardApi.getToday().catch(() => ({})),
      planApi.list(todayStr).catch(() => [])
    ])
    todayData.value = dashboard || {}
    todayPlans.value = plans || []

    // 异步加载每日一句
    excerptApi.getRandom().then(res => {
      randomExcerpt.value = res
    }).catch(() => {})
  } catch (err) {
    console.error('Dashboard load error:', err)
  }
}

// 切换计划状态
async function togglePlanStatus(item: PlanItem) {
  const newStatus = item.status === 'DONE' ? 'TODO' : 'DONE'
  try {
    await planApi.updateStatus(item.id, newStatus)
    item.status = newStatus as PlanItem['status']
    // 更新统计
    if (newStatus === 'DONE') {
      todayData.value.planDone = (todayData.value.planDone || 0) + 1
    } else {
      todayData.value.planDone = Math.max(0, (todayData.value.planDone || 1) - 1)
    }
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function goTab(name: string) {
  const map: Record<string, string> = {
    plan: '/pages/plan/plan',
    accounting: '/pages/accounting/accounting',
    excerpt: '/pages/excerpt/excerpt',
    profile: '/pages/profile/profile'
  }
  uni.switchTab({ url: map[name] || '/pages/index/index' })
}

function goProfile() {
  uni.switchTab({ url: '/pages/profile/profile' })
}

function navigateTo(url: string) {
  uni.navigateTo({ url })
}

onShow(() => {
  if (userStore.isLoggedIn) {
    loadData()
  }
})
</script>

<style lang="scss" scoped>
// 头部
.header-greeting {
  .greeting-emoji {
    font-size: 40rpx;
  }
}

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;

  .header-date {
    font-size: $dt-font-md;
    font-weight: 600;
    color: $dt-text-primary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
  }
  .header-weekday {
    font-size: $dt-font-xs;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }
}

.header-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $dt-primary, $dt-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;

  .avatar-text {
    color: #fff;
    font-size: 28rpx;
    font-weight: 600;
  }
}

// 欢迎横幅
.welcome-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, $dt-primary, #818CF8);
  border-radius: $dt-radius-lg;
  padding: 36rpx 32rpx;
  margin-bottom: $dt-space-lg;

  .welcome-text {
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }

  .welcome-hi {
    font-size: $dt-font-xl;
    font-weight: 700;
    color: #FFFFFF;
  }

  .welcome-sub {
    font-size: $dt-font-sm;
    color: rgba(255, 255, 255, 0.8);
  }

  .welcome-emoji {
    font-size: 80rpx;
  }
}

// 数据网格
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  margin-bottom: $dt-space-lg;
}

.stat-card {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 28rpx;
  box-shadow: $dt-shadow-sm;
  position: relative;
  overflow: hidden;

  @media (prefers-color-scheme: dark) {
    background: $dt-dark-bg-card;
  }

  &__icon-wrap {
    margin-bottom: 12rpx;
  }

  &__icon {
    font-size: 44rpx;
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
  }

  &__value {
    font-size: $dt-font-xl;
    font-weight: 700;
    color: $dt-text-primary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
  }

  &__label {
    font-size: $dt-font-xs;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &__progress {
    margin-top: 16rpx;
    height: 6rpx;
    background: #E5E7EB;
    border-radius: 3rpx;
    overflow: hidden;

    &-bar {
      height: 100%;
      background: $dt-primary;
      border-radius: 3rpx;
      transition: width 0.6s ease;
    }
  }
}

// 快捷操作
.quick-actions {
  display: flex;
  justify-content: space-around;
}

.quick-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx;

  &__icon {
    width: 96rpx;
    height: 96rpx;
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 44rpx;
    transition: transform 0.2s;

    &:active {
      transform: scale(0.9);
    }
  }

  &__text {
    font-size: $dt-font-xs;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }
}

// 卡片更多链接
.card-more {
  font-size: $dt-font-sm;
  color: $dt-primary;
  font-weight: 500;
}

// 计划项
.plan-item {
  display: flex;
  align-items: flex-start;
  padding: 20rpx 0;
  gap: 20rpx;

  & + .plan-item {
    border-top: 1rpx solid $dt-divider;
    @media (prefers-color-scheme: dark) { border-top-color: $dt-dark-border; }
  }

  &__check {
    width: 44rpx;
    height: 44rpx;
    border-radius: 50%;
    border: 3rpx solid $dt-border;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-top: 4rpx;
    transition: all 0.3s;

    &--done {
      background: $dt-success;
      border-color: $dt-success;
    }

    &-icon {
      color: #FFFFFF;
      font-size: 24rpx;
      font-weight: 700;
    }
  }

  &__content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }

  &__title {
    font-size: $dt-font-md;
    color: $dt-text-primary;
    font-weight: 500;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }

    &--done {
      text-decoration: line-through;
      color: $dt-text-placeholder;
    }
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  &__tag {
    font-size: 20rpx;
    padding: 2rpx 12rpx;
    border-radius: 6rpx;
    font-weight: 500;

    &.tag-p0 { background: rgba(239, 68, 68, 0.1); color: #EF4444; }
    &.tag-p1 { background: rgba(245, 158, 11, 0.1); color: #F59E0B; }
    &.tag-p2 { background: rgba(99, 102, 241, 0.1); color: #6366F1; }
    &.tag-p3 { background: rgba(107, 114, 128, 0.1); color: #6B7280; }
  }

  &__category {
    font-size: $dt-font-xs;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }
}

// 迷你空状态
.mini-empty {
  padding: 40rpx 0;
  text-align: center;

  &__text {
    font-size: $dt-font-sm;
    color: $dt-text-placeholder;
  }
}

// 每日一句
.quote-card {
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  border-radius: $dt-radius-lg;
  padding: 32rpx;
  margin-bottom: $dt-space-lg;
  position: relative;

  @media (prefers-color-scheme: dark) {
    background: linear-gradient(135deg, rgba(254, 243, 199, 0.15), rgba(253, 230, 138, 0.1));
  }

  .quote-mark {
    font-size: 80rpx;
    color: rgba(245, 158, 11, 0.3);
    line-height: 1;
    position: absolute;
    top: 8rpx;
    left: 16rpx;
    font-family: Georgia, serif;
  }

  .quote-content {
    font-size: $dt-font-md;
    color: #92400E;
    line-height: 1.8;
    @media (prefers-color-scheme: dark) { color: #FBBF24; }
  }

  .quote-source {
    display: block;
    margin-top: 16rpx;
    font-size: $dt-font-sm;
    color: #B45309;
    text-align: right;
    @media (prefers-color-scheme: dark) { color: #F59E0B; }
  }
}
</style>
