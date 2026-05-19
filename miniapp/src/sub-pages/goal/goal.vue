<template>
  <view class="page-container">
    <dt-navbar title="目标管理" show-back />

    <view class="page-content">
      <!-- 统计卡片 -->
      <view class="stats-row fade-in">
        <view class="stat-chip">
          <text class="stat-num">{{ allGoals.length }}</text>
          <text class="stat-label">总目标</text>
        </view>
        <view class="stat-chip stat-chip--success">
          <text class="stat-num text-success">{{ completedCount }}</text>
          <text class="stat-label">已完成</text>
        </view>
        <view class="stat-chip stat-chip--primary">
          <text class="stat-num text-primary">{{ inProgressCount }}</text>
          <text class="stat-label">进行中</text>
        </view>
      </view>

      <!-- 周期类型筛选 -->
      <scroll-view scroll-x class="type-scroll">
        <view class="type-tabs">
          <view v-for="t in goalTypes" :key="t.value"
            class="type-tab"
            :class="{ 'type-tab--active': activeType === t.value }"
            @tap="activeType = t.value">
            <text class="type-tab-text">{{ t.icon }} {{ t.label }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 目标列表 -->
      <view v-if="filteredGoals.length > 0" class="goal-list">
        <view v-for="(goal, idx) in filteredGoals" :key="goal.id"
          class="goal-card slide-up"
          :style="{ animationDelay: (idx * 0.06) + 's' }">

          <!-- 状态标签 -->
          <view class="goal-status-badge" :class="'badge-' + goal.status.toLowerCase()">
            <text class="badge-text">{{ statusLabel(goal.status) }}</text>
          </view>

          <view class="goal-header">
            <text class="goal-title">{{ goal.title }}</text>
            <text class="goal-type-tag">{{ typeLabel(goal.goalType) }}</text>
          </view>

          <view v-if="goal.description" class="goal-desc">
            <text class="goal-desc-text text-ellipsis-2">{{ goal.description }}</text>
          </view>

          <!-- 进度条 -->
          <view class="goal-progress">
            <view class="progress-header">
              <text class="progress-label">进度</text>
              <text class="progress-value" :class="goal.progress >= 80 ? 'text-success' : 'text-primary'">
                {{ goal.progress || 0 }}%
              </text>
            </view>
            <view class="progress-track">
              <view class="progress-fill"
                :style="{ width: (goal.progress || 0) + '%' }"
                :class="goal.progress >= 100 ? 'fill-success' : 'fill-primary'" />
            </view>
          </view>

          <!-- 时间范围 -->
          <view class="goal-dates">
            <text class="goal-date">📅 {{ goal.startDate }} ~ {{ goal.endDate }}</text>
          </view>

          <!-- 关键结果 -->
          <view v-if="goal.keyResults && goal.keyResults.length > 0" class="goal-krs">
            <text class="kr-title">关键结果</text>
            <view v-for="kr in goal.keyResults" :key="kr.id || kr.title" class="kr-item">
              <view class="kr-info">
                <text class="kr-text">{{ kr.title }}</text>
                <text class="kr-progress">{{ kr.currentValue }} / {{ kr.targetValue }} {{ kr.unit }}</text>
              </view>
              <view class="kr-bar">
                <view class="kr-bar-fill"
                  :style="{ width: Math.min(100, (kr.currentValue / kr.targetValue * 100)) + '%' }" />
              </view>
            </view>
          </view>
        </view>
      </view>

      <dt-empty v-else icon="🎯" text="还没有目标，快去添加吧" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { goalApi, type GoalItem } from '@/api/goal'

const allGoals = ref<GoalItem[]>([])
const activeType = ref('ALL')

const goalTypes = [
  { icon: '🌟', label: '全部', value: 'ALL' },
  { icon: '🏔', label: '五年', value: 'FIVE_YEAR' },
  { icon: '📆', label: '年度', value: 'YEARLY' },
  { icon: '📅', label: '月度', value: 'MONTHLY' },
  { icon: '📋', label: '周度', value: 'WEEKLY' }
]

const completedCount = computed(() =>
  allGoals.value.filter(g => g.status === 'COMPLETED').length
)
const inProgressCount = computed(() =>
  allGoals.value.filter(g => g.status === 'IN_PROGRESS').length
)
const filteredGoals = computed(() => {
  if (activeType.value === 'ALL') return allGoals.value
  return allGoals.value.filter(g => g.goalType === activeType.value)
})

function statusLabel(status: string) {
  const map: Record<string, string> = {
    NOT_STARTED: '未开始', IN_PROGRESS: '进行中',
    COMPLETED: '已完成', ABANDONED: '已放弃'
  }
  return map[status] || status
}

function typeLabel(type: string) {
  const map: Record<string, string> = {
    FIVE_YEAR: '五年目标', YEARLY: '年度目标',
    MONTHLY: '月度目标', WEEKLY: '周计划'
  }
  return map[type] || type
}

async function loadGoals() {
  const res = await goalApi.list({}).catch(() => [])
  allGoals.value = res || []
}

onShow(() => {
  loadGoals()
})
</script>

<style lang="scss" scoped>
// 统计行
.stats-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: $dt-space-lg;
}

.stat-chip {
  flex: 1;
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.stat-num {
  font-size: $dt-font-2xl;
  font-weight: 700;
  color: $dt-text-primary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.stat-label {
  font-size: $dt-font-xs;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

// 类型筛选
.type-scroll { margin-bottom: $dt-space-lg; white-space: nowrap; }

.type-tabs {
  display: inline-flex;
  gap: 12rpx;
}

.type-tab {
  display: inline-flex;
  padding: 14rpx 28rpx;
  border-radius: $dt-radius-full;
  background: $dt-bg-card;
  box-shadow: $dt-shadow-sm;
  white-space: nowrap;
  transition: all 0.2s;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &--active {
    background: $dt-primary;
    box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.3);
  }

  &-text {
    font-size: $dt-font-sm;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &--active &-text {
    color: #FFFFFF;
    font-weight: 500;
  }
}

// 目标卡片
.goal-list {
  padding-bottom: 80rpx;
}

.goal-card {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: $dt-shadow-sm;
  position: relative;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.goal-status-badge {
  display: inline-flex;
  padding: 4rpx 16rpx;
  border-radius: $dt-radius-full;
  margin-bottom: 16rpx;

  &.badge-in_progress { background: rgba(99, 102, 241, 0.1); }
  &.badge-completed { background: rgba(16, 185, 129, 0.1); }
  &.badge-not_started { background: rgba(107, 114, 128, 0.1); }
  &.badge-abandoned { background: rgba(239, 68, 68, 0.1); }

  .badge-text {
    font-size: 22rpx;
    font-weight: 500;
  }
}

.badge-in_progress .badge-text { color: $dt-primary; }
.badge-completed .badge-text { color: $dt-success; }
.badge-not_started .badge-text { color: $dt-info; }
.badge-abandoned .badge-text { color: $dt-danger; }

.goal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.goal-title {
  font-size: $dt-font-lg;
  font-weight: 600;
  color: $dt-text-primary;
  flex: 1;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.goal-type-tag {
  font-size: 22rpx;
  color: $dt-text-secondary;
  background: rgba(0, 0, 0, 0.04);
  padding: 4rpx 16rpx;
  border-radius: $dt-radius-full;
  white-space: nowrap;
  @media (prefers-color-scheme: dark) {
    background: rgba(255, 255, 255, 0.06);
    color: $dt-dark-text-secondary;
  }
}

.goal-desc {
  margin-bottom: 16rpx;

  &-text {
    font-size: $dt-font-sm;
    color: $dt-text-secondary;
    line-height: 1.6;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }
}

// 进度条
.goal-progress {
  margin-bottom: 16rpx;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.progress-label {
  font-size: $dt-font-sm;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.progress-value {
  font-size: $dt-font-sm;
  font-weight: 600;
}

.progress-track {
  height: 8rpx;
  background: $dt-divider;
  border-radius: 4rpx;
  overflow: hidden;
  @media (prefers-color-scheme: dark) { background: $dt-dark-border; }
}

.progress-fill {
  height: 100%;
  border-radius: 4rpx;
  transition: width 0.6s ease;

  &.fill-primary { background: linear-gradient(90deg, $dt-primary, $dt-primary-light); }
  &.fill-success { background: linear-gradient(90deg, $dt-success, $dt-success-light); }
}

.goal-dates {
  margin-bottom: 16rpx;

  .goal-date {
    font-size: $dt-font-xs;
    color: $dt-text-placeholder;
  }
}

// 关键结果
.goal-krs {
  border-top: 1rpx solid $dt-divider;
  padding-top: 16rpx;
  @media (prefers-color-scheme: dark) { border-top-color: $dt-dark-border; }
}

.kr-title {
  font-size: $dt-font-sm;
  color: $dt-text-secondary;
  font-weight: 500;
  display: block;
  margin-bottom: 12rpx;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.kr-item {
  margin-bottom: 12rpx;
}

.kr-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6rpx;
}

.kr-text {
  font-size: $dt-font-sm;
  color: $dt-text-primary;
  flex: 1;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.kr-progress {
  font-size: $dt-font-xs;
  color: $dt-primary;
  font-weight: 500;
}

.kr-bar {
  height: 6rpx;
  background: $dt-divider;
  border-radius: 3rpx;
  overflow: hidden;
  @media (prefers-color-scheme: dark) { background: $dt-dark-border; }

  &-fill {
    height: 100%;
    background: linear-gradient(90deg, $dt-primary, $dt-primary-light);
    border-radius: 3rpx;
    transition: width 0.5s ease;
  }
}
</style>
