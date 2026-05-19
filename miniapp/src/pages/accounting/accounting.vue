<template>
  <view class="page-container">
    <dt-navbar title="记账">
      <template #right>
        <view class="nav-add-btn" @tap="goAdd">
          <text class="nav-add-icon">+</text>
        </view>
      </template>
    </dt-navbar>

    <view class="page-content">
      <!-- 月度收支概览 -->
      <view class="balance-card fade-in">
        <view class="balance-header">
          <text class="balance-month">{{ currentMonth }}</text>
          <text class="balance-label">本月结余</text>
          <text class="balance-amount" :class="monthStats.balance >= 0 ? 'amount-positive' : 'amount-negative'">
            ¥{{ formatMoney(monthStats.balance || 0) }}
          </text>
        </view>
        <view class="balance-divider" />
        <view class="balance-footer">
          <view class="balance-item">
            <view class="balance-item-row">
              <text class="balance-item-icon">↑</text>
              <text class="balance-item-value text-success">¥{{ formatMoney(monthStats.totalIncome || 0) }}</text>
            </view>
            <text class="balance-item-label">收入</text>
          </view>
          <view class="balance-sep" />
          <view class="balance-item">
            <view class="balance-item-row">
              <text class="balance-item-icon expense">↓</text>
              <text class="balance-item-value text-danger">¥{{ formatMoney(monthStats.totalExpense || 0) }}</text>
            </view>
            <text class="balance-item-label">支出</text>
          </view>
        </view>
      </view>

      <!-- 类型筛选 -->
      <view class="type-tabs">
        <view v-for="t in typeTabs" :key="t.value"
          class="type-tab"
          :class="{ 'type-tab--active': activeType === t.value, [`type-tab--${t.color}`]: activeType === t.value }"
          @tap="activeType = t.value; loadRecords()">
          <text class="type-tab__text">{{ t.label }}</text>
        </view>
      </view>

      <!-- 记录列表 -->
      <view v-if="records.length > 0" class="records-list">
        <view v-for="(item, idx) in records" :key="item.id"
          class="record-item slide-up"
          :style="{ animationDelay: (idx * 0.04) + 's' }">

          <!-- 类别图标 -->
          <view class="record-icon" :class="item.type === 'INCOME' ? 'icon-income' : 'icon-expense'">
            <text class="record-emoji">{{ getCategoryEmoji(item.categoryName) }}</text>
          </view>

          <!-- 信息区 -->
          <view class="record-info">
            <text class="record-category">{{ item.categoryName }}</text>
            <view class="record-meta">
              <text class="record-time">{{ formatTime(item.accountingTime) }}</text>
              <text v-if="item.remark" class="record-remark">{{ item.remark }}</text>
            </view>
          </view>

          <!-- 金额 -->
          <view class="record-right">
            <text class="record-amount" :class="item.type === 'INCOME' ? 'text-success' : 'text-danger'">
              {{ item.type === 'INCOME' ? '+' : '-' }}¥{{ formatMoney(item.amount) }}
            </text>
            <text class="record-account">{{ item.accountType || '现金' }}</text>
          </view>

          <!-- 滑动删除区域 -->
          <view class="record-delete" @tap.stop="deleteRecord(item)">
            <text class="delete-icon">🗑</text>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="hasMore" class="load-more" @tap="loadMore">
          <text class="load-more-text">加载更多</text>
        </view>
        <view v-else class="list-end">
          <text class="list-end-text">— 已全部加载 —</text>
        </view>
      </view>

      <dt-empty v-else icon="💰" text="还没有记账记录" action-text="记一笔" @action="goAdd" />
    </view>

    <!-- 浮动添加按钮 -->
    <view class="fab-button" @tap="goAdd">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { accountingApi, type AccountingItem, type AccountingStats } from '@/api/accounting'
import { formatMoney } from '@/utils/date'
import dayjs from 'dayjs'

const currentMonth = computed(() => dayjs().format('YYYY年M月'))
const activeType = ref<'ALL' | 'INCOME' | 'EXPENSE'>('ALL')

const typeTabs = [
  { label: '全部', value: 'ALL', color: 'primary' },
  { label: '收入', value: 'INCOME', color: 'success' },
  { label: '支出', value: 'EXPENSE', color: 'danger' }
]

const monthStats = ref<Partial<AccountingStats>>({})
const records = ref<AccountingItem[]>([])
const pageNum = ref(1)
const hasMore = ref(false)
const pageSize = 20

// 类别 Emoji 映射
const emojiMap: Record<string, string> = {
  '餐饮': '🍜', '食品': '🛒', '交通': '🚌', '购物': '🛍', '娱乐': '🎮',
  '医疗': '💊', '教育': '📚', '居家': '🏠', '服装': '👕', '美容': '💄',
  '运动': '🏃', '旅游': '✈️', '通讯': '📱', '工资': '💼', '奖金': '🎉',
  '兼职': '💻', '理财': '📈', '其他': '📦'
}

function getCategoryEmoji(name: string): string {
  for (const [k, v] of Object.entries(emojiMap)) {
    if (name?.includes(k)) return v
  }
  return '📦'
}

function formatTime(t: string | null): string {
  if (!t) return ''
  return t.substring(0, 5)
}

async function loadStats() {
  const now = dayjs()
  monthStats.value = await accountingApi.monthlyStats(now.year(), now.month() + 1)
    .catch(() => ({}))
}

async function loadRecords() {
  pageNum.value = 1
  const now = dayjs()
  const params: any = {
    pageNum: 1,
    pageSize,
    startDate: now.startOf('month').format('YYYY-MM-DD'),
    endDate: now.endOf('month').format('YYYY-MM-DD')
  }
  if (activeType.value !== 'ALL') params.type = activeType.value

  const result = await accountingApi.page(params).catch(() => null)
  if (result) {
    records.value = result.records || []
    hasMore.value = result.pageNum < result.pages
  }
}

async function loadMore() {
  pageNum.value++
  const now = dayjs()
  const params: any = {
    pageNum: pageNum.value,
    pageSize,
    startDate: now.startOf('month').format('YYYY-MM-DD'),
    endDate: now.endOf('month').format('YYYY-MM-DD')
  }
  if (activeType.value !== 'ALL') params.type = activeType.value

  const result = await accountingApi.page(params).catch(() => null)
  if (result) {
    records.value.push(...(result.records || []))
    hasMore.value = result.pageNum < result.pages
  }
}

function deleteRecord(item: AccountingItem) {
  uni.showModal({
    title: '删除记录',
    content: `确定删除「${item.categoryName} ¥${item.amount}」？`,
    success: async (res) => {
      if (res.confirm) {
        await accountingApi.delete(item.id).catch(() => null)
        records.value = records.value.filter(r => r.id !== item.id)
        loadStats()
        uni.showToast({ title: '已删除', icon: 'success' })
      }
    }
  })
}

function goAdd() {
  uni.navigateTo({ url: '/sub-pages/accounting-add/accounting-add' })
}

onShow(() => {
  loadStats()
  loadRecords()
})
</script>

<style lang="scss" scoped>
// 收支卡片
.balance-card {
  background: linear-gradient(135deg, #6366F1 0%, #818CF8 100%);
  border-radius: $dt-radius-xl;
  padding: 40rpx 32rpx 32rpx;
  margin-bottom: $dt-space-lg;
  color: #FFFFFF;
  box-shadow: 0 8rpx 32rpx rgba(99, 102, 241, 0.35);
}

.balance-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 32rpx;
}

.balance-month {
  font-size: $dt-font-sm;
  color: rgba(255, 255, 255, 0.7);
}

.balance-label {
  font-size: $dt-font-md;
  color: rgba(255, 255, 255, 0.85);
}

.balance-amount {
  font-size: $dt-font-3xl;
  font-weight: 700;
  letter-spacing: -2rpx;
}

.amount-positive { color: #A7F3D0; }
.amount-negative { color: #FECACA; }

.balance-divider {
  height: 1rpx;
  background: rgba(255, 255, 255, 0.2);
  margin-bottom: 28rpx;
}

.balance-footer {
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.balance-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;

  &-row {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  &-icon {
    font-size: 28rpx;
    color: rgba(167, 243, 208, 1);
    font-weight: 700;

    &.expense {
      color: rgba(254, 202, 202, 1);
    }
  }

  &-value {
    font-size: $dt-font-xl;
    font-weight: 700;
    color: #FFFFFF;
  }

  &-label {
    font-size: $dt-font-xs;
    color: rgba(255, 255, 255, 0.7);
  }
}

.balance-sep {
  width: 1rpx;
  height: 64rpx;
  background: rgba(255, 255, 255, 0.2);
}

// 类型标签
.type-tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: $dt-space-lg;
}

.type-tab {
  flex: 1;
  height: 72rpx;
  border-radius: $dt-radius-md;
  background: $dt-bg-card;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $dt-shadow-sm;
  transition: all 0.25s;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &--active.type-tab--primary {
    background: linear-gradient(135deg, $dt-primary, $dt-primary-light);
    box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.3);
  }
  &--active.type-tab--success {
    background: linear-gradient(135deg, $dt-success, $dt-success-light);
    box-shadow: 0 4rpx 16rpx rgba(16, 185, 129, 0.3);
  }
  &--active.type-tab--danger {
    background: linear-gradient(135deg, $dt-danger, $dt-danger-light);
    box-shadow: 0 4rpx 16rpx rgba(239, 68, 68, 0.3);
  }

  &__text {
    font-size: $dt-font-md;
    color: $dt-text-secondary;
    font-weight: 500;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &--active &__text {
    color: #FFFFFF;
  }
}

// 记录列表
.records-list {
  padding-bottom: 120rpx;
}

.record-item {
  display: flex;
  align-items: center;
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 24rpx;
  margin-bottom: 16rpx;
  gap: 20rpx;
  box-shadow: $dt-shadow-sm;
  position: relative;
  overflow: hidden;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.record-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: $dt-radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.icon-income {
    background: rgba(16, 185, 129, 0.1);
  }
  &.icon-expense {
    background: rgba(239, 68, 68, 0.1);
  }
}

.record-emoji {
  font-size: 40rpx;
}

.record-info {
  flex: 1;
  min-width: 0;
}

.record-category {
  font-size: $dt-font-md;
  font-weight: 500;
  color: $dt-text-primary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.record-meta {
  display: flex;
  gap: 12rpx;
  margin-top: 6rpx;
}

.record-time, .record-remark {
  font-size: $dt-font-xs;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.record-remark {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}

.record-amount {
  font-size: $dt-font-lg;
  font-weight: 700;
}

.record-account {
  font-size: $dt-font-xs;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.record-delete {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 0;
  background: $dt-danger;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.load-more, .list-end {
  padding: 32rpx 0;
  text-align: center;
}

.load-more-text {
  font-size: $dt-font-sm;
  color: $dt-primary;
}

.list-end-text {
  font-size: $dt-font-xs;
  color: $dt-text-placeholder;
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

// FAB
.fab-button {
  position: fixed;
  right: 32rpx;
  bottom: calc(180rpx + env(safe-area-inset-bottom));
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $dt-success, $dt-success-light);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(16, 185, 129, 0.4);
  z-index: 100;

  &:active { transform: scale(0.9); }

  .fab-icon {
    color: #FFFFFF;
    font-size: 56rpx;
    font-weight: 300;
    line-height: 1;
  }
}
</style>
