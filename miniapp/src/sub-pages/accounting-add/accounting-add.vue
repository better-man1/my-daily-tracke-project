<template>
  <view class="page-container">
    <dt-navbar title="记一笔" show-back />

    <view class="page-content">
      <!-- 收支类型切换 -->
      <view class="type-switch fade-in">
        <view class="type-btn"
          :class="{ 'type-btn--expense': form.type === 'EXPENSE' }"
          @tap="form.type = 'EXPENSE'; loadCategories()">
          <text class="type-icon">↓</text>
          <text class="type-text">支出</text>
        </view>
        <view class="type-btn"
          :class="{ 'type-btn--income': form.type === 'INCOME' }"
          @tap="form.type = 'INCOME'; loadCategories()">
          <text class="type-icon">↑</text>
          <text class="type-text">收入</text>
        </view>
      </view>

      <!-- 金额输入 -->
      <view class="amount-card fade-in">
        <text class="amount-label">金额</text>
        <view class="amount-row">
          <text class="amount-currency">¥</text>
          <input
            class="amount-input"
            v-model="form.amount"
            type="digit"
            placeholder="0.00"
            placeholder-class="amount-placeholder"
            focus
          />
        </view>
        <!-- 快速金额选项 -->
        <view class="quick-amounts">
          <view v-for="a in quickAmounts" :key="a"
            class="quick-amount-btn"
            @tap="form.amount = String(a)">
            <text class="quick-amount-text">{{ a }}</text>
          </view>
        </view>
      </view>

      <!-- 分类选择 -->
      <view class="form-card slide-up">
        <text class="section-title">选择分类</text>
        <view class="category-grid">
          <view v-for="cat in categories" :key="cat.id"
            class="category-item"
            :class="{ 'category-item--active': form.categoryId === cat.id }"
            @tap="form.categoryId = cat.id">
            <view class="category-icon" :class="{ 'category-icon--active': form.categoryId === cat.id }">
              <text class="cat-emoji">{{ getCatEmoji(cat.name) }}</text>
            </view>
            <text class="category-name">{{ cat.name }}</text>
          </view>
        </view>
      </view>

      <!-- 其他信息 -->
      <view class="form-card slide-up">
        <!-- 日期 -->
        <view class="info-row">
          <text class="info-label">📅 日期</text>
          <picker mode="date" :value="form.accountingDate"
            @change="form.accountingDate = $event.detail.value">
            <text class="info-value">{{ form.accountingDate }}</text>
          </picker>
        </view>
        <view class="info-divider" />

        <!-- 时间 -->
        <view class="info-row">
          <text class="info-label">⏰ 时间</text>
          <picker mode="time" :value="form.accountingTime || ''"
            @change="form.accountingTime = $event.detail.value">
            <text class="info-value">{{ form.accountingTime || '现在' }}</text>
          </picker>
        </view>
        <view class="info-divider" />

        <!-- 账户类型 -->
        <view class="info-row">
          <text class="info-label">💳 账户</text>
          <picker :range="accountTypes" :value="accountTypeIndex"
            @change="accountTypeIndex = $event.detail.value">
            <text class="info-value">{{ accountTypes[accountTypeIndex] }}</text>
          </picker>
        </view>
        <view class="info-divider" />

        <!-- 备注 -->
        <view class="info-row info-row--col">
          <text class="info-label">📝 备注</text>
          <input
            class="remark-input"
            v-model="form.remark"
            placeholder="添加备注（可选）"
            placeholder-class="input-placeholder"
            maxlength="100"
          />
        </view>
      </view>

      <!-- 提交 -->
      <button class="submit-btn" :loading="loading" @tap="handleSubmit">
        <text class="submit-text">记录</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { accountingApi } from '@/api/accounting'
import { getToday } from '@/utils/date'
import dayjs from 'dayjs'

const loading = ref(false)
const categories = ref<any[]>([])
const accountTypes = ['现金', '微信', '支付宝', '银行卡', '信用卡', '其他']
const accountTypeIndex = ref(0)
const quickAmounts = [10, 20, 50, 100, 200, 500]

const form = ref({
  type: 'EXPENSE' as 'INCOME' | 'EXPENSE',
  amount: '',
  categoryId: 0,
  accountingDate: getToday(),
  accountingTime: dayjs().format('HH:mm'),
  remark: ''
})

const emojiMap: Record<string, string> = {
  '餐饮': '🍜', '食品': '🛒', '交通': '🚌', '购物': '🛍', '娱乐': '🎮',
  '医疗': '💊', '教育': '📚', '居家': '🏠', '服装': '👕', '美容': '💄',
  '运动': '🏃', '旅游': '✈️', '通讯': '📱', '工资': '💼', '奖金': '🎉',
  '兼职': '💻', '理财': '📈', '其他': '📦'
}

function getCatEmoji(name: string): string {
  for (const [k, v] of Object.entries(emojiMap)) {
    if (name?.includes(k)) return v
  }
  return '📦'
}

async function loadCategories() {
  const res = await accountingApi.getCategories(form.value.type).catch(() => [])
  categories.value = res || []
  if (categories.value.length > 0 && !form.value.categoryId) {
    form.value.categoryId = categories.value[0].id
  }
}

async function handleSubmit() {
  const amount = parseFloat(form.value.amount)
  if (!amount || amount <= 0) {
    uni.showToast({ title: '请输入有效金额', icon: 'none' })
    return
  }
  if (!form.value.categoryId) {
    uni.showToast({ title: '请选择分类', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await accountingApi.create({
      type: form.value.type,
      amount,
      categoryId: form.value.categoryId,
      accountType: accountTypes[accountTypeIndex.value],
      remark: form.value.remark || undefined,
      accountingDate: form.value.accountingDate,
      accountingTime: form.value.accountingTime || undefined
    })
    uni.showToast({ title: '记录成功 🎉', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 800)
  } catch {
    uni.showToast({ title: '记录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style lang="scss" scoped>
// 类型切换
.type-switch {
  display: flex;
  gap: 16rpx;
  margin-bottom: $dt-space-lg;
}

.type-btn {
  flex: 1;
  height: 96rpx;
  border-radius: $dt-radius-lg;
  background: $dt-bg-card;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: $dt-shadow-sm;
  transition: all 0.25s;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &--expense {
    background: linear-gradient(135deg, $dt-danger, $dt-danger-light);
    box-shadow: 0 6rpx 20rpx rgba(239, 68, 68, 0.3);

    .type-icon, .type-text { color: #FFFFFF !important; }
  }

  &--income {
    background: linear-gradient(135deg, $dt-success, $dt-success-light);
    box-shadow: 0 6rpx 20rpx rgba(16, 185, 129, 0.3);

    .type-icon, .type-text { color: #FFFFFF !important; }
  }

  .type-icon {
    font-size: 36rpx;
    font-weight: 700;
    color: $dt-text-secondary;
  }

  .type-text {
    font-size: $dt-font-lg;
    font-weight: 600;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }
}

// 金额卡片
.amount-card {
  background: $dt-bg-card;
  border-radius: $dt-radius-xl;
  padding: 32rpx;
  margin-bottom: $dt-space-lg;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.amount-label {
  font-size: $dt-font-sm;
  color: $dt-text-secondary;
  display: block;
  margin-bottom: 12rpx;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.amount-row {
  display: flex;
  align-items: center;
  border-bottom: 2rpx solid $dt-divider;
  padding-bottom: 16rpx;
  margin-bottom: 24rpx;

  @media (prefers-color-scheme: dark) { border-bottom-color: $dt-dark-border; }
}

.amount-currency {
  font-size: $dt-font-3xl;
  font-weight: 700;
  color: $dt-text-primary;
  margin-right: 8rpx;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.amount-input {
  flex: 1;
  font-size: $dt-font-3xl;
  font-weight: 700;
  color: $dt-text-primary;
  background: transparent;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.amount-placeholder {
  font-size: $dt-font-3xl;
  font-weight: 700;
  color: $dt-text-placeholder;
}

.quick-amounts {
  display: flex;
  gap: 12rpx;
}

.quick-amount-btn {
  flex: 1;
  height: 56rpx;
  background: rgba(99, 102, 241, 0.08);
  border-radius: $dt-radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active { background: rgba(99, 102, 241, 0.18); }

  .quick-amount-text {
    font-size: $dt-font-sm;
    color: $dt-primary;
    font-weight: 500;
  }
}

// 分类网格
.section-title {
  font-size: $dt-font-md;
  font-weight: 600;
  color: $dt-text-primary;
  display: block;
  margin-bottom: 24rpx;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;

  &:active { opacity: 0.7; }
}

.category-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: $dt-radius-md;
  background: rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  border: 2rpx solid transparent;

  @media (prefers-color-scheme: dark) { background: rgba(255, 255, 255, 0.06); }

  &--active {
    border-color: $dt-primary;
    background: $dt-primary-bg;
    box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.2);
  }

  .cat-emoji { font-size: 44rpx; }
}

.category-name {
  font-size: $dt-font-xs;
  color: $dt-text-secondary;
  text-align: center;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.category-item.category-item--active .category-name {
  color: $dt-primary;
  font-weight: 500;
}

// 信息卡片
.form-card {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 8rpx 32rpx;
  margin-bottom: $dt-space-lg;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  min-height: 80rpx;

  &--col {
    flex-direction: column;
    align-items: flex-start;
    gap: 12rpx;
  }
}

.info-label {
  font-size: $dt-font-md;
  color: $dt-text-primary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.info-value {
  font-size: $dt-font-md;
  color: $dt-primary;
  font-weight: 500;
}

.info-divider {
  height: 1rpx;
  background: $dt-divider;
  @media (prefers-color-scheme: dark) { background: $dt-dark-border; }
}

.remark-input {
  width: 100%;
  font-size: $dt-font-md;
  color: $dt-text-primary;
  background: transparent;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.input-placeholder {
  color: $dt-text-placeholder;
  font-size: $dt-font-md;
}

// 提交
.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, $dt-success, $dt-success-light);
  border-radius: $dt-radius-md;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(16, 185, 129, 0.3);
  margin-bottom: 60rpx;

  &::after { border: none; }
  &:active { opacity: 0.85; }

  .submit-text {
    color: #FFFFFF;
    font-size: $dt-font-xl;
    font-weight: 700;
    letter-spacing: 8rpx;
  }
}
</style>
