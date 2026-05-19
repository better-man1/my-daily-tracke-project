<template>
  <view class="page-container">
    <dt-navbar title="计划管理">
      <template #right>
        <view class="nav-add-btn" @tap="goAdd">
          <text class="nav-add-icon">+</text>
        </view>
      </template>
    </dt-navbar>

    <view class="page-content">
      <!-- 日期选择器 -->
      <view class="date-selector">
        <view class="date-arrow" @tap="changeDate(-1)">
          <text class="arrow-icon">‹</text>
        </view>
        <view class="date-display" @tap="showDatePicker">
          <text class="date-text">{{ displayDate }}</text>
          <text class="date-weekday">{{ weekdayStr }}</text>
        </view>
        <view class="date-arrow" @tap="changeDate(1)">
          <text class="arrow-icon">›</text>
        </view>
        <view v-if="currentDate !== today" class="date-today-btn" @tap="goToday">
          <text class="date-today-text">今天</text>
        </view>
      </view>

      <!-- 统计栏 -->
      <view v-if="stats" class="stats-bar fade-in">
        <view class="stats-item">
          <text class="stats-value">{{ stats.total }}</text>
          <text class="stats-label">总计</text>
        </view>
        <view class="stats-divider" />
        <view class="stats-item">
          <text class="stats-value text-success">{{ stats.done }}</text>
          <text class="stats-label">已完成</text>
        </view>
        <view class="stats-divider" />
        <view class="stats-item">
          <text class="stats-value text-primary">{{ stats.inProgress }}</text>
          <text class="stats-label">进行中</text>
        </view>
        <view class="stats-divider" />
        <view class="stats-item">
          <text class="stats-value text-warning">{{ stats.todo }}</text>
          <text class="stats-label">待办</text>
        </view>
        <view class="stats-divider" />
        <view class="stats-item">
          <text class="stats-value" :class="rateClass">{{ stats.completionRate || 0 }}%</text>
          <text class="stats-label">完成率</text>
        </view>
      </view>

      <!-- 筛选标签 -->
      <scroll-view scroll-x class="filter-scroll">
        <view class="filter-tags">
          <view v-for="f in filters" :key="f.value"
            class="filter-tag"
            :class="{ 'filter-tag--active': activeFilter === f.value }"
            @tap="activeFilter = f.value">
            <text class="filter-tag__text">{{ f.label }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 计划列表 -->
      <view v-if="filteredPlans.length > 0" class="plan-list">
        <view v-for="(item, idx) in filteredPlans" :key="item.id"
          class="plan-card slide-up"
          :style="{ animationDelay: (idx * 0.05) + 's' }">

          <!-- 优先级指示条 -->
          <view class="plan-card__priority-bar" :class="'priority-' + item.priority.toLowerCase()" />

          <view class="plan-card__main">
            <!-- 复选框 -->
            <view class="plan-card__check"
              :class="{ 'plan-card__check--done': item.status === 'DONE' }"
              @tap.stop="toggleStatus(item)">
              <text v-if="item.status === 'DONE'" class="check-icon">✓</text>
            </view>

            <!-- 内容 -->
            <view class="plan-card__body" @tap="goDetail(item)">
              <text class="plan-card__title"
                :class="{ 'plan-card__title--done': item.status === 'DONE' }">
                {{ item.title }}
              </text>
              <view class="plan-card__info">
                <text class="plan-card__tag"
                  :class="'tag-' + item.priority.toLowerCase()">
                  {{ item.priority }}
                </text>
                <text class="plan-card__cat">{{ categoryLabel(item.category) }}</text>
                <text v-if="item.estimatedMins" class="plan-card__time">
                  ⏱ {{ item.estimatedMins }}min
                </text>
              </view>
            </view>

            <!-- 操作按钮 -->
            <view class="plan-card__actions">
              <view v-if="item.status !== 'DONE'" class="action-btn" @tap.stop="postponePlan(item)">
                <text class="action-icon">→</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <dt-empty v-else icon="📋" text="今天还没有计划" action-text="添加计划" @action="goAdd" />
    </view>

    <!-- 浮动添加按钮 -->
    <view class="fab-button safe-area-bottom" @tap="goAdd">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { planApi, type PlanItem, type PlanStatistics } from '@/api/plan'
import { formatDate, getWeekDay, formatDateCN } from '@/utils/date'
import dayjs from 'dayjs'

const today = formatDate()
const currentDate = ref(today)
const plans = ref<PlanItem[]>([])
const stats = ref<PlanStatistics | null>(null)
const activeFilter = ref('ALL')

const filters = [
  { label: '全部', value: 'ALL' },
  { label: '待办', value: 'TODO' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]

const displayDate = computed(() => {
  if (currentDate.value === today) return '今天'
  const d = dayjs(currentDate.value)
  const yesterday = dayjs().subtract(1, 'day').format('YYYY-MM-DD')
  const tomorrow = dayjs().add(1, 'day').format('YYYY-MM-DD')
  if (currentDate.value === yesterday) return '昨天'
  if (currentDate.value === tomorrow) return '明天'
  return formatDateCN(currentDate.value)
})

const weekdayStr = computed(() => getWeekDay(currentDate.value))

const rateClass = computed(() => {
  const rate = stats.value?.completionRate || 0
  if (rate >= 80) return 'text-success'
  if (rate >= 50) return 'text-primary'
  return 'text-warning'
})

const filteredPlans = computed(() => {
  if (activeFilter.value === 'ALL') return plans.value
  return plans.value.filter(p => p.status === activeFilter.value)
})

const categoryLabel = (cat: string) => {
  const map: Record<string, string> = { WORK: '工作', STUDY: '学习', LIFE: '生活', HEALTH: '健康' }
  return map[cat] || cat
}

function changeDate(delta: number) {
  currentDate.value = dayjs(currentDate.value).add(delta, 'day').format('YYYY-MM-DD')
  loadData()
}

function goToday() {
  currentDate.value = today
  loadData()
}

function showDatePicker() {
  // 使用 uni 内置日期选择器
}

async function loadData() {
  try {
    const [planList, planStats] = await Promise.all([
      planApi.list(currentDate.value),
      planApi.statistics(currentDate.value)
    ])
    plans.value = planList || []
    stats.value = planStats || null
  } catch (err) {
    console.error('Load plans error:', err)
  }
}

async function toggleStatus(item: PlanItem) {
  const newStatus = item.status === 'DONE' ? 'TODO' : 'DONE'
  try {
    await planApi.updateStatus(item.id, newStatus)
    item.status = newStatus as PlanItem['status']
    // 刷新统计
    const s = await planApi.statistics(currentDate.value)
    stats.value = s
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function postponePlan(item: PlanItem) {
  uni.showModal({
    title: '延期计划',
    content: `确定将「${item.title}」延期到明天吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await planApi.postpone(item.id)
          uni.showToast({ title: '已延期', icon: 'success' })
          loadData()
        } catch {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

function goAdd() {
  uni.navigateTo({
    url: `/sub-pages/plan-add/plan-add?date=${currentDate.value}`
  })
}

function goDetail(item: PlanItem) {
  // 简单编辑——复用新增页面
  uni.navigateTo({
    url: `/sub-pages/plan-add/plan-add?id=${item.id}&date=${currentDate.value}`
  })
}

onShow(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
// 日期选择器
.date-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24rpx;
  padding: 20rpx 0;
  margin-bottom: 16rpx;
}

.date-arrow {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $dt-bg-card;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  .arrow-icon {
    font-size: 44rpx;
    color: $dt-text-secondary;
    font-weight: 300;
    line-height: 1;
  }
}

.date-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 160rpx;

  .date-text {
    font-size: $dt-font-xl;
    font-weight: 700;
    color: $dt-text-primary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
  }

  .date-weekday {
    font-size: $dt-font-xs;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }
}

.date-today-btn {
  padding: 8rpx 24rpx;
  background: $dt-primary-bg;
  border-radius: $dt-radius-full;

  .date-today-text {
    font-size: $dt-font-xs;
    color: $dt-primary;
    font-weight: 500;
  }
}

// 统计栏
.stats-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 24rpx 16rpx;
  margin-bottom: $dt-space-lg;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.stats-value {
  font-size: $dt-font-lg;
  font-weight: 700;
  color: $dt-text-primary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.stats-label {
  font-size: $dt-font-xs;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.stats-divider {
  width: 1rpx;
  height: 48rpx;
  background: $dt-divider;
  @media (prefers-color-scheme: dark) { background: $dt-dark-border; }
}

// 筛选标签
.filter-scroll {
  white-space: nowrap;
  margin-bottom: $dt-space-lg;
}

.filter-tags {
  display: inline-flex;
  gap: 16rpx;
  padding: 0 4rpx;
}

.filter-tag {
  display: inline-flex;
  padding: 12rpx 28rpx;
  border-radius: $dt-radius-full;
  background: $dt-bg-card;
  box-shadow: $dt-shadow-sm;
  transition: all 0.2s;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &--active {
    background: $dt-primary;
    box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.3);
  }

  &__text {
    font-size: $dt-font-sm;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &--active &__text {
    color: #FFFFFF;
    font-weight: 500;
  }
}

// 计划卡片
.plan-list {
  padding-bottom: 120rpx;
}

.plan-card {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  margin-bottom: 16rpx;
  overflow: hidden;
  box-shadow: $dt-shadow-sm;
  position: relative;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &__priority-bar {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx;

    &.priority-p0 { background: $dt-danger; }
    &.priority-p1 { background: $dt-warning; }
    &.priority-p2 { background: $dt-primary; }
    &.priority-p3 { background: $dt-info; }
  }

  &__main {
    display: flex;
    align-items: center;
    padding: 28rpx 24rpx 28rpx 32rpx;
    gap: 20rpx;
  }

  &__check {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    border: 3rpx solid $dt-border;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: all 0.3s ease;

    &--done {
      background: $dt-success;
      border-color: $dt-success;
    }

    .check-icon {
      color: #fff;
      font-size: 26rpx;
      font-weight: 700;
    }
  }

  &__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    min-width: 0;
  }

  &__title {
    font-size: $dt-font-md;
    font-weight: 500;
    color: $dt-text-primary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }

    &--done {
      text-decoration: line-through;
      color: $dt-text-placeholder;
    }
  }

  &__info {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  &__tag {
    font-size: 20rpx;
    padding: 2rpx 12rpx;
    border-radius: 6rpx;
    font-weight: 500;

    &.tag-p0 { background: rgba(239, 68, 68, 0.1); color: $dt-danger; }
    &.tag-p1 { background: rgba(245, 158, 11, 0.1); color: $dt-warning; }
    &.tag-p2 { background: rgba(99, 102, 241, 0.1); color: $dt-primary; }
    &.tag-p3 { background: rgba(107, 114, 128, 0.1); color: $dt-info; }
  }

  &__cat, &__time {
    font-size: $dt-font-xs;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &__actions {
    flex-shrink: 0;
  }
}

.action-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(245, 158, 11, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;

  .action-icon {
    font-size: 28rpx;
    color: $dt-warning;
  }
}

// 导航栏添加按钮
.nav-add-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: $dt-primary;
  display: flex;
  align-items: center;
  justify-content: center;

  .nav-add-icon {
    color: #fff;
    font-size: 36rpx;
    font-weight: 300;
    line-height: 1;
  }
}

// 浮动按钮
.fab-button {
  position: fixed;
  right: 32rpx;
  bottom: calc(180rpx + env(safe-area-inset-bottom));
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $dt-primary, $dt-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.4);
  z-index: 100;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.9);
  }

  .fab-icon {
    color: #FFFFFF;
    font-size: 56rpx;
    font-weight: 300;
    line-height: 1;
  }
}
</style>
