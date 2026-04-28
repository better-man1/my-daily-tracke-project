<template>
  <view class="page-container">
    <view class="page-header">
      <view>
        <text class="page-title">每日计划</text>
        <text class="page-subtitle">{{ selectedDate }} · {{ stats.done ?? 0 }}/{{ stats.total ?? 0 }} 完成</text>
      </view>
      <view class="header-actions">
        <picker mode="date" :value="selectedDate" @change="onDateChange">
          <view class="date-picker-btn">📅</view>
        </picker>
        <view class="add-btn" @tap="showAddModal = true">＋</view>
      </view>
    </view>

    <!-- 进度条 -->
    <view class="completion-bar card">
      <view class="completion-header">
        <text class="text-secondary text-sm">完成进度</text>
        <text class="text-purple font-bold">{{ stats.completionRate ?? 0 }}%</text>
      </view>
      <view class="progress-bar">
        <view class="progress-fill" :style="{ width: (stats.completionRate ?? 0) + '%', background: '#6366f1' }" />
      </view>
      <view class="status-counts">
        <view v-for="s in statusInfo" :key="s.label" class="status-item">
          <view class="status-dot-mini" :style="{ background: s.color }" />
          <text class="text-xs text-secondary">{{ s.label }} {{ s.count }}</text>
        </view>
      </view>
    </view>

    <!-- 任务分类 -->
    <scroll-view scroll-x class="category-tabs">
      <view
        v-for="cat in categories"
        :key="cat.value"
        class="cat-tab"
        :class="{ active: selectedCat === cat.value }"
        @tap="selectedCat = cat.value"
      >
        {{ cat.label }}
      </view>
    </scroll-view>

    <!-- 任务列表 -->
    <view class="task-list">
      <view v-if="loading" class="loading-tip"><text>加载中...</text></view>
      <view
        v-for="plan in filteredPlans"
        :key="plan.id"
        class="task-item"
        :class="{ 'is-done': plan.status === 'DONE', 'is-cancelled': plan.status === 'CANCELLED' }"
      >
        <!-- 完成按钮 -->
        <view class="task-check" :class="plan.status" @tap="toggleStatus(plan)">
          <text v-if="plan.status === 'DONE'" style="color:white;font-size:22rpx">✓</text>
          <text v-else-if="plan.status === 'IN_PROGRESS'" style="color:#f59e0b;font-size:20rpx">●</text>
        </view>

        <!-- 内容 -->
        <view class="task-content">
          <view class="task-title-row">
            <text class="task-title">{{ plan.title }}</text>
            <text class="priority-badge" :class="`p-${plan.priority.toLowerCase()}`">{{ plan.priority }}</text>
          </view>
          <view v-if="plan.description" class="task-desc">{{ plan.description }}</view>
          <view class="task-meta">
            <text class="cat-badge">{{ catLabel(plan.category) }}</text>
            <text v-if="plan.estimatedMins" class="text-xs text-muted">⏱ {{ plan.estimatedMins }}min</text>
          </view>
        </view>

        <!-- 删除 -->
        <view class="task-del" @tap="deletePlan(plan.id)">✕</view>
      </view>

      <view v-if="!filteredPlans.length" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">{{ selectedCat ? '该分类暂无任务' : '今天还没有任务' }}</text>
      </view>
    </view>

    <!-- 新增任务弹窗 -->
    <uni-popup ref="popup" type="bottom" background-color="#1a1d27" border-radius="24rpx 24rpx 0 0">
      <view v-if="showAddModal" class="add-modal">
        <view class="modal-header">
          <text class="modal-title">新增任务</text>
          <text class="modal-close" @tap="showAddModal = false">✕</text>
        </view>

        <view class="form-item">
          <text class="form-label">任务标题 *</text>
          <input v-model="form.title" class="form-input" placeholder="输入任务标题" placeholder-style="color:#475569" />
        </view>

        <view class="form-item">
          <text class="form-label">描述（可选）</text>
          <textarea v-model="form.description" class="form-textarea" placeholder="输入任务描述" placeholder-style="color:#475569" />
        </view>

        <view class="form-row">
          <view class="form-item half-item">
            <text class="form-label">优先级</text>
            <picker :range="priorityOptions" range-key="label" :value="priorityIdx" @change="e => priorityIdx = e.detail.value">
              <view class="picker-btn">{{ priorityOptions[priorityIdx].label }}</view>
            </picker>
          </view>
          <view class="form-item half-item">
            <text class="form-label">分类</text>
            <picker :range="categories" range-key="label" :value="catIdx" @change="e => catIdx = e.detail.value">
              <view class="picker-btn">{{ categories[catIdx].label }}</view>
            </picker>
          </view>
        </view>

        <button class="submit-btn" :loading="saving" @tap="savePlan">确认添加</button>
      </view>
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { planApi } from '../../api/index'

const selectedDate = ref(new Date().toISOString().split('T')[0])
const selectedCat = ref('')
const plans = ref<any[]>([])
const stats = ref<any>({})
const loading = ref(false)
const saving = ref(false)
const showAddModal = ref(false)
const popup = ref<any>()

const categories = [
  { value: '', label: '全部' },
  { value: 'WORK', label: '工作' },
  { value: 'STUDY', label: '学习' },
  { value: 'LIFE', label: '生活' },
  { value: 'HEALTH', label: '健康' }
]

const priorityOptions = [
  { value: 'P0', label: 'P0 紧急' },
  { value: 'P1', label: 'P1 重要' },
  { value: 'P2', label: 'P2 普通' },
  { value: 'P3', label: 'P3 低优' }
]

const priorityIdx = ref(2)
const catIdx = ref(1)

const form = reactive({ title: '', description: '', estimatedMins: 0 })

const filteredPlans = computed(() =>
  selectedCat.value ? plans.value.filter(p => p.category === selectedCat.value) : plans.value
)

const statusInfo = computed(() => [
  { label: '待完成', count: plans.value.filter(p => p.status === 'TODO').length, color: '#64748b' },
  { label: '进行中', count: plans.value.filter(p => p.status === 'IN_PROGRESS').length, color: '#f59e0b' },
  { label: '已完成', count: plans.value.filter(p => p.status === 'DONE').length, color: '#10b981' }
])

function catLabel(v: string) { return categories.find(c => c.value === v)?.label ?? v }

async function loadData() {
  loading.value = true
  try {
    ;[plans.value, stats.value] = await Promise.all([
      planApi.list(selectedDate.value),
      planApi.statistics(selectedDate.value)
    ])
  } finally {
    loading.value = false
  }
}

function onDateChange(e: any) {
  selectedDate.value = e.detail.value
  loadData()
}

async function toggleStatus(plan: any) {
  const nextMap: Record<string, string> = { TODO: 'IN_PROGRESS', IN_PROGRESS: 'DONE', DONE: 'TODO', CANCELLED: 'TODO' }
  await planApi.updateStatus(plan.id, nextMap[plan.status])
  plan.status = nextMap[plan.status]
  stats.value = await planApi.statistics(selectedDate.value)
}

async function deletePlan(id: number) {
  const result = await uni.showModal({ title: '提示', content: '确认删除？', confirmText: '删除', confirmColor: '#ef4444' })
  if (result.confirm) {
    await planApi.delete(id)
    await loadData()
  }
}

async function savePlan() {
  if (!form.title.trim()) { uni.showToast({ title: '请输入标题', icon: 'none' }); return }
  saving.value = true
  try {
    await planApi.create({
      ...form,
      planDate: selectedDate.value,
      priority: priorityOptions[priorityIdx.value].value,
      category: categories[catIdx.value].value || 'WORK'
    })
    uni.showToast({ title: '创建成功', icon: 'success' })
    showAddModal.value = false
    form.title = ''
    form.description = ''
    await loadData()
  } finally {
    saving.value = false
  }
}

watch(showAddModal, (v) => { v ? popup.value?.open() : popup.value?.close() })

onMounted(loadData)
onShow(loadData)
</script>

<style lang="scss" scoped>
.page-container { background: #0f1117; min-height: 100vh; padding: 24rpx; padding-bottom: 120rpx; }

.page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 24rpx;
  .page-title { font-size: 44rpx; font-weight: 700; color: #e2e8f0; display: block; }
  .page-subtitle { font-size: 24rpx; color: #94a3b8; display: block; margin-top: 6rpx; }
  .header-actions { display: flex; align-items: center; gap: 16rpx; }
  .date-picker-btn { font-size: 48rpx; }
  .add-btn { width: 72rpx; height: 72rpx; border-radius: 50%; background: #6366f1; color: white; display: flex; align-items: center; justify-content: center; font-size: 40rpx; font-weight: 300; }
}

.completion-bar { margin-bottom: 20rpx;
  .completion-header { display: flex; justify-content: space-between; margin-bottom: 12rpx; }
  .progress-bar { height: 10rpx; background: rgba(255,255,255,0.08); border-radius: 5rpx; overflow: hidden; margin-bottom: 16rpx; }
  .progress-fill { height: 100%; border-radius: 5rpx; }
  .status-counts { display: flex; gap: 24rpx; }
  .status-item { display: flex; align-items: center; gap: 8rpx; }
  .status-dot-mini { width: 12rpx; height: 12rpx; border-radius: 50%; }
}

.category-tabs { display: flex; margin-bottom: 20rpx;
  .cat-tab { flex-shrink: 0; padding: 12rpx 28rpx; border-radius: 100rpx; font-size: 26rpx; color: #64748b; margin-right: 12rpx; background: rgba(255,255,255,0.05);
    &.active { background: rgba(99,102,241,0.2); color: #818cf8; }
  }
}

.task-list { display: flex; flex-direction: column; gap: 12rpx; }

.task-item { display: flex; align-items: flex-start; gap: 20rpx; background: #1a1d27; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 20rpx; padding: 24rpx;
  &.is-done { opacity: 0.55; .task-title { text-decoration: line-through; color: #64748b; } }
  &.is-cancelled { opacity: 0.35; }
  .task-check { width: 48rpx; height: 48rpx; border-radius: 50%; border: 2rpx solid #64748b; display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-top: 4rpx;
    &.DONE { background: #10b981; border-color: #10b981; }
    &.IN_PROGRESS { border-color: #f59e0b; }
    &.CANCELLED { border-color: #ef4444; }
  }
  .task-content { flex: 1; min-width: 0; }
  .task-title-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8rpx; }
  .task-title { font-size: 28rpx; color: #e2e8f0; font-weight: 500; flex: 1; }
  .priority-badge { font-size: 22rpx; font-weight: 600; padding: 4rpx 12rpx; border-radius: 8rpx; background: rgba(255,255,255,0.06);
    &.p-p0 { color: #ef4444; } &.p-p1 { color: #f59e0b; } &.p-p2 { color: #818cf8; } &.p-p3 { color: #64748b; }
  }
  .task-desc { font-size: 24rpx; color: #64748b; margin-bottom: 8rpx; }
  .task-meta { display: flex; align-items: center; gap: 12rpx; }
  .cat-badge { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 8rpx; background: rgba(255,255,255,0.06); color: #94a3b8; }
  .task-del { color: #475569; font-size: 28rpx; padding: 8rpx; flex-shrink: 0; }
}

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80rpx 24rpx;
  .empty-icon { font-size: 96rpx; margin-bottom: 20rpx; }
  .empty-text { font-size: 28rpx; color: #64748b; }
}

.add-modal { padding: 40rpx;
  .modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 32rpx; }
  .modal-title { font-size: 36rpx; font-weight: 700; color: #e2e8f0; }
  .modal-close { font-size: 36rpx; color: #64748b; }
  .form-item { margin-bottom: 24rpx; }
  .form-label { display: block; font-size: 26rpx; color: #94a3b8; margin-bottom: 12rpx; }
  .form-input { width: 100%; background: #252836; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 28rpx; color: #e2e8f0; box-sizing: border-box; }
  .form-textarea { width: 100%; background: #252836; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 28rpx; color: #e2e8f0; box-sizing: border-box; height: 120rpx; }
  .form-row { display: flex; gap: 20rpx; }
  .half-item { flex: 1; }
  .picker-btn { background: #252836; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 28rpx; color: #e2e8f0; }
  .submit-btn { width: 100%; height: 96rpx; background: linear-gradient(135deg, #6366f1, #7c3aed); color: white; border-radius: 20rpx; font-size: 32rpx; font-weight: 600; border: none; margin-top: 16rpx; }
}
</style>
