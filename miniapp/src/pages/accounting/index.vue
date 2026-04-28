<template>
  <view class="page-container">
    <view class="page-header">
      <view>
        <text class="page-title">每日记账</text>
        <text class="page-subtitle">{{ currentMonth }}月收支管理</text>
      </view>
      <view class="add-btn" @tap="showAddModal = true">＋</view>
    </view>

    <!-- 月度统计 -->
    <view class="month-stats">
      <view class="month-card income">
        <text class="month-icon">💰</text>
        <text class="month-amount">¥{{ fmt(monthStats.totalIncome) }}</text>
        <text class="month-label">本月收入</text>
      </view>
      <view class="month-card expense">
        <text class="month-icon">💸</text>
        <text class="month-amount">¥{{ fmt(monthStats.totalExpense) }}</text>
        <text class="month-label">本月支出</text>
      </view>
      <view class="month-card balance">
        <text class="month-icon">{{ monthStats.balance >= 0 ? '📈' : '📉' }}</text>
        <text class="month-amount" :style="{ color: monthStats.balance >= 0 ? '#10b981' : '#ef4444' }">
          ¥{{ fmt(Math.abs(monthStats.balance ?? 0)) }}
        </text>
        <text class="month-label">{{ monthStats.balance >= 0 ? '结余' : '超支' }}</text>
      </view>
    </view>

    <!-- 类型筛选 -->
    <view class="type-tabs">
      <view class="type-tab" :class="{ active: filterType === '' }" @tap="filterType = ''; loadList()">全部</view>
      <view class="type-tab" :class="{ active: filterType === 'EXPENSE' }" @tap="filterType = 'EXPENSE'; loadList()">支出</view>
      <view class="type-tab" :class="{ active: filterType === 'INCOME' }" @tap="filterType = 'INCOME'; loadList()">收入</view>
    </view>

    <!-- 账目列表 -->
    <view class="acc-list">
      <view v-for="item in list" :key="item.id" class="acc-item card">
        <view class="acc-type-dot" :class="item.type" />
        <view class="acc-info">
          <text class="acc-category">{{ item.categoryName }}</text>
          <text class="acc-date text-muted text-xs">{{ item.accountingDate }}</text>
          <text v-if="item.remark" class="acc-remark text-secondary text-sm">{{ item.remark }}</text>
        </view>
        <view class="acc-right">
          <text class="acc-amount" :class="item.type">
            {{ item.type === 'INCOME' ? '+' : '-' }}¥{{ fmt(item.amount) }}
          </text>
          <text class="acc-del" @tap="deleteItem(item.id)">✕</text>
        </view>
      </view>

      <view v-if="!list.length" class="empty-state">
        <text class="empty-icon">💰</text>
        <text class="empty-text">暂无账目记录</text>
      </view>
    </view>

    <!-- 新增弹窗 -->
    <uni-popup ref="popup" type="bottom" background-color="#1a1d27">
      <view v-if="showAddModal" class="add-modal">
        <view class="modal-header">
          <text class="modal-title">记一笔</text>
          <text class="modal-close" @tap="showAddModal = false">✕</text>
        </view>

        <!-- 收入/支出切换 -->
        <view class="type-switch">
          <view :class="['type-btn', form.type === 'EXPENSE' ? 'expense-active' : '']" @tap="form.type = 'EXPENSE'">支出</view>
          <view :class="['type-btn', form.type === 'INCOME' ? 'income-active' : '']" @tap="form.type = 'INCOME'">收入</view>
        </view>

        <!-- 金额输入 -->
        <view class="amount-input-area">
          <text class="amount-symbol">¥</text>
          <input v-model.number="form.amount" class="amount-input" type="digit" placeholder="0.00" placeholder-style="color:#475569" />
        </view>

        <view class="form-item">
          <text class="form-label">备注</text>
          <input v-model="form.remark" class="form-input" placeholder="备注（可选）" placeholder-style="color:#475569" />
        </view>

        <view class="form-item">
          <text class="form-label">日期</text>
          <picker mode="date" :value="form.accountingDate" @change="e => form.accountingDate = e.detail.value">
            <view class="picker-btn">{{ form.accountingDate }}</view>
          </picker>
        </view>

        <!-- 快捷分类 -->
        <view class="form-label">分类</view>
        <scroll-view scroll-x class="cat-scroll">
          <view
            v-for="cat in currentCategories"
            :key="cat.id"
            class="cat-chip"
            :class="{ selected: form.categoryId === cat.id }"
            @tap="form.categoryId = cat.id"
          >
            {{ cat.name }}
          </view>
        </scroll-view>

        <button class="submit-btn" :loading="saving" @tap="saveItem">确认记账</button>
      </view>
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { accountingApi } from '../../api/index'

const list = ref<any[]>([])
const monthStats = ref<any>({ totalIncome: 0, totalExpense: 0, balance: 0 })
const categories = ref<any[]>([])
const filterType = ref('')
const showAddModal = ref(false)
const saving = ref(false)
const popup = ref<any>()

const now = new Date()
const currentMonth = now.getMonth() + 1

const form = reactive({
  type: 'EXPENSE' as 'EXPENSE' | 'INCOME',
  amount: 0,
  categoryId: 0,
  remark: '',
  accountingDate: now.toISOString().split('T')[0],
  accountType: 'WECHAT'
})

const currentCategories = computed(() => {
  const parent = categories.value.find(c => c.type === form.type)
  if (!parent) return categories.value.filter(c => c.type === form.type)
  const subs: any[] = []
  categories.value.forEach((c: any) => { if (c.type === form.type) subs.push(c, ...(c.children ?? [])) })
  return categories.value.filter((c: any) => c.type === form.type)
})

function fmt(v: any) { return Number(v ?? 0).toFixed(2) }

async function loadList() {
  const res = await accountingApi.page({ pageNum: 1, pageSize: 30, type: filterType.value || undefined })
  list.value = res.records ?? []
}

async function loadMonthStats() {
  monthStats.value = await accountingApi.monthlyStats(now.getFullYear(), now.getMonth() + 1)
}

async function loadCategories() {
  categories.value = await accountingApi.getCategories()
}

async function deleteItem(id: number) {
  const r = await uni.showModal({ title: '提示', content: '确认删除？', confirmText: '删除', confirmColor: '#ef4444' })
  if (r.confirm) { await accountingApi.delete(id); loadList(); loadMonthStats() }
}

async function saveItem() {
  if (!form.amount || form.amount <= 0) { uni.showToast({ title: '请输入金额', icon: 'none' }); return }
  if (!form.categoryId) { uni.showToast({ title: '请选择分类', icon: 'none' }); return }
  saving.value = true
  try {
    await accountingApi.create({ ...form })
    uni.showToast({ title: '记录成功', icon: 'success' })
    showAddModal.value = false
    form.amount = 0; form.remark = ''; form.categoryId = 0
    await Promise.all([loadList(), loadMonthStats()])
  } finally { saving.value = false }
}

watch(showAddModal, (v) => { v ? popup.value?.open() : popup.value?.close() })

onMounted(() => { loadList(); loadMonthStats(); loadCategories() })
onShow(() => { loadList(); loadMonthStats() })
</script>

<style lang="scss" scoped>
.page-container { background: #0f1117; min-height: 100vh; padding: 24rpx; padding-bottom: 120rpx; }

.page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 24rpx;
  .page-title { font-size: 44rpx; font-weight: 700; color: #e2e8f0; display: block; }
  .page-subtitle { font-size: 24rpx; color: #94a3b8; }
  .add-btn { width: 72rpx; height: 72rpx; border-radius: 50%; background: #10b981; color: white; display: flex; align-items: center; justify-content: center; font-size: 40rpx; }
}

.month-stats { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12rpx; margin-bottom: 24rpx;
  .month-card { background: #1a1d27; border-radius: 20rpx; padding: 24rpx 16rpx; text-align: center; border: 1rpx solid rgba(255,255,255,0.08);
    .month-icon { font-size: 40rpx; display: block; margin-bottom: 8rpx; }
    .month-amount { display: block; font-size: 28rpx; font-weight: 700; color: #e2e8f0; }
    .month-label { display: block; font-size: 22rpx; color: #94a3b8; margin-top: 6rpx; }
  }
}

.type-tabs { display: flex; gap: 16rpx; margin-bottom: 20rpx;
  .type-tab { padding: 12rpx 32rpx; border-radius: 100rpx; font-size: 26rpx; color: #64748b; background: rgba(255,255,255,0.05);
    &.active { background: rgba(99,102,241,0.2); color: #818cf8; }
  }
}

.acc-list { display: flex; flex-direction: column; gap: 12rpx; }

.acc-item { display: flex; align-items: flex-start; gap: 16rpx; padding: 24rpx;
  .acc-type-dot { width: 12rpx; height: 12rpx; border-radius: 50%; margin-top: 12rpx; flex-shrink: 0;
    &.INCOME { background: #10b981; } &.EXPENSE { background: #ef4444; }
  }
  .acc-info { flex: 1; min-width: 0; }
  .acc-category { font-size: 28rpx; color: #e2e8f0; display: block; }
  .acc-right { display: flex; flex-direction: column; align-items: flex-end; gap: 12rpx; }
  .acc-amount { font-size: 32rpx; font-weight: 700;
    &.INCOME { color: #10b981; } &.EXPENSE { color: #ef4444; }
  }
  .acc-del { color: #475569; font-size: 28rpx; }
}

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80rpx 24rpx;
  .empty-icon { font-size: 96rpx; margin-bottom: 20rpx; }
  .empty-text { font-size: 28rpx; color: #64748b; }
}

.add-modal { padding: 40rpx;
  .modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 32rpx; }
  .modal-title { font-size: 36rpx; font-weight: 700; color: #e2e8f0; }
  .modal-close { font-size: 36rpx; color: #64748b; }
  .type-switch { display: flex; background: rgba(255,255,255,0.05); border-radius: 16rpx; padding: 6rpx; margin-bottom: 32rpx;
    .type-btn { flex: 1; text-align: center; padding: 16rpx; border-radius: 12rpx; font-size: 30rpx; color: #64748b;
      &.expense-active { background: rgba(239,68,68,0.2); color: #ef4444; }
      &.income-active { background: rgba(16,185,129,0.2); color: #10b981; }
    }
  }
  .amount-input-area { display: flex; align-items: center; background: #252836; border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx;
    .amount-symbol { font-size: 44rpx; color: #94a3b8; margin-right: 12rpx; }
    .amount-input { flex: 1; font-size: 48rpx; font-weight: 700; color: #e2e8f0; }
  }
  .form-item { margin-bottom: 20rpx; }
  .form-label { display: block; font-size: 26rpx; color: #94a3b8; margin-bottom: 12rpx; }
  .form-input { width: 100%; background: #252836; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 28rpx; color: #e2e8f0; box-sizing: border-box; }
  .picker-btn { background: #252836; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 28rpx; color: #e2e8f0; }
  .cat-scroll { display: flex; margin-bottom: 32rpx; }
  .cat-chip { flex-shrink: 0; padding: 12rpx 28rpx; border-radius: 100rpx; font-size: 26rpx; color: #64748b; background: rgba(255,255,255,0.05); margin-right: 12rpx;
    &.selected { background: rgba(16,185,129,0.2); color: #10b981; }
  }
  .submit-btn { width: 100%; height: 96rpx; background: linear-gradient(135deg, #10b981, #059669); color: white; border-radius: 20rpx; font-size: 32rpx; font-weight: 600; border: none; }
}
</style>
