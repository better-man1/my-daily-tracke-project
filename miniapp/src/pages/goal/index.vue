<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-title">目标管理</text>
      <view class="add-btn" @tap="openAdd">＋</view>
    </view>

    <!-- 类型过滤 -->
    <scroll-view scroll-x class="type-tabs">
      <view v-for="t in goalTypes" :key="t.value" class="type-tab" :class="{ active: selectedType === t.value }" @tap="selectedType = t.value; loadGoals()">
        {{ t.label }}
      </view>
    </scroll-view>

    <!-- 目标列表 -->
    <view class="goal-list">
      <view v-for="goal in goals" :key="goal.id" class="goal-card card">
        <view class="goal-header">
          <view class="goal-type-badge">{{ typeLabel(goal.goalType) }}</view>
          <view class="goal-status" :class="goal.status.toLowerCase()">{{ statusLabel(goal.status) }}</view>
        </view>
        <text class="goal-title">{{ goal.title }}</text>
        <text v-if="goal.description" class="goal-desc text-secondary text-sm">{{ goal.description }}</text>

        <!-- 进度条 -->
        <view class="goal-progress">
          <view class="progress-header">
            <text class="text-sm text-secondary">完成进度</text>
            <text class="text-sm font-bold" :style="{ color: progressColor(goal.progress) }">{{ goal.progress }}%</text>
          </view>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: goal.progress + '%', background: progressColor(goal.progress) }" />
          </view>
          <!-- 进度快捷调整 -->
          <view class="progress-controls">
            <view v-for="v in [0, 25, 50, 75, 100]" :key="v" class="progress-btn" :class="{ active: goal.progress === v }" @tap="setProgress(goal, v)">{{ v }}%</view>
          </view>
        </view>

        <view class="goal-timeline">
          <text class="text-xs text-muted">{{ goal.startDate }} → {{ goal.endDate }}</text>
        </view>
      </view>

      <view v-if="!goals.length" class="empty-state">
        <text class="empty-icon">🎯</text>
        <text class="empty-text">还没有目标，设定你的第一个目标</text>
      </view>
    </view>

    <!-- 新增弹窗 -->
    <uni-popup ref="popup" type="bottom" background-color="#1a1d27">
      <view v-if="showForm" class="add-modal">
        <view class="modal-header">
          <text class="modal-title">新增目标</text>
          <text class="modal-close" @tap="showForm = false">✕</text>
        </view>

        <view class="form-item">
          <text class="form-label">目标标题 *</text>
          <input v-model="form.title" class="form-input" placeholder="输入目标标题" placeholder-style="color:#475569" />
        </view>

        <view class="form-item">
          <text class="form-label">目标类型</text>
          <view class="goal-types">
            <view v-for="t in goalTypes.slice(1)" :key="t.value" class="type-chip" :class="{ active: form.goalType === t.value }" @tap="form.goalType = t.value">
              {{ t.label }}
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">描述（可选）</text>
          <textarea v-model="form.description" class="form-textarea" placeholder="描述你的目标..." placeholder-style="color:#475569" />
        </view>

        <view class="form-row">
          <view class="half-item">
            <text class="form-label">开始日期</text>
            <picker mode="date" :value="form.startDate" @change="e => form.startDate = e.detail.value">
              <view class="picker-btn">{{ form.startDate }}</view>
            </picker>
          </view>
          <view class="half-item">
            <text class="form-label">截止日期</text>
            <picker mode="date" :value="form.endDate" @change="e => form.endDate = e.detail.value">
              <view class="picker-btn">{{ form.endDate }}</view>
            </picker>
          </view>
        </view>

        <button class="submit-btn" :loading="saving" @tap="save">创建目标</button>
      </view>
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { goalApi } from '../../api/index'

// 原生日期工具（替代 dayjs，兼容小程序）
function getYearStart() { const y = new Date().getFullYear(); return `${y}-01-01` }
function getYearEnd()   { const y = new Date().getFullYear(); return `${y}-12-31` }

const goals = ref<any[]>([])
const selectedType = ref('')
const showForm = ref(false)
const saving = ref(false)
const popup = ref<any>()

const goalTypes = [
  { value: '', label: '全部' },
  { value: 'FIVE_YEAR', label: '五年' },
  { value: 'YEARLY', label: '年度' },
  { value: 'MONTHLY', label: '月度' },
  { value: 'WEEKLY', label: '周' }
]

const statusMap: Record<string, string> = { NOT_STARTED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', ABANDONED: '已放弃' }

const form = reactive({
  title: '',
  description: '',
  goalType: 'YEARLY',
  startDate: getYearStart(),
  endDate: getYearEnd(),
  progress: 0
})

function typeLabel(v: string) { return goalTypes.find(t => t.value === v)?.label ?? v }
function statusLabel(v: string) { return statusMap[v] ?? v }
function progressColor(p: number) {
  if (p >= 80) return '#10b981'
  if (p >= 50) return '#6366f1'
  if (p >= 20) return '#f59e0b'
  return '#64748b'
}

async function loadGoals() {
  goals.value = await goalApi.list(selectedType.value ? { goalType: selectedType.value } : undefined)
}

async function setProgress(goal: any, v: number) {
  await goalApi.updateProgress(goal.id, v)
  goal.progress = v
}

function openAdd() {
  form.title = ''; form.description = ''; form.goalType = 'YEARLY'; form.progress = 0
  form.startDate = getYearStart()
  form.endDate = getYearEnd()
  showForm.value = true
}

async function save() {
  if (!form.title.trim()) { uni.showToast({ title: '请输入标题', icon: 'none' }); return }
  saving.value = true
  try {
    await goalApi.create({ ...form })
    uni.showToast({ title: '创建成功', icon: 'success' })
    showForm.value = false
    loadGoals()
  } finally { saving.value = false }
}

watch(showForm, (v) => { v ? popup.value?.open() : popup.value?.close() })
onMounted(loadGoals)
</script>

<style lang="scss" scoped>
.page-container { background: #0f1117; min-height: 100vh; padding: 24rpx; padding-bottom: 40rpx; }

.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24rpx;
  .page-title { font-size: 44rpx; font-weight: 700; color: #e2e8f0; }
  .add-btn { width: 72rpx; height: 72rpx; border-radius: 50%; background: #3b82f6; color: white; display: flex; align-items: center; justify-content: center; font-size: 40rpx; }
}

.type-tabs { display: flex; margin-bottom: 24rpx;
  .type-tab { flex-shrink: 0; padding: 12rpx 28rpx; border-radius: 100rpx; font-size: 26rpx; color: #64748b; margin-right: 12rpx; background: rgba(255,255,255,0.05);
    &.active { background: rgba(59,130,246,0.2); color: #60a5fa; }
  }
}

.goal-list { display: flex; flex-direction: column; gap: 16rpx; }

.goal-card { padding: 28rpx;
  .goal-header { display: flex; align-items: center; gap: 12rpx; margin-bottom: 16rpx; }
  .goal-type-badge { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 100rpx; background: rgba(59,130,246,0.15); color: #60a5fa; }
  .goal-status { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 100rpx;
    &.not_started { background: rgba(100,116,139,0.15); color: #64748b; }
    &.in_progress { background: rgba(245,158,11,0.15); color: #f59e0b; }
    &.completed { background: rgba(16,185,129,0.15); color: #10b981; }
    &.abandoned { background: rgba(239,68,68,0.15); color: #ef4444; }
  }
  .goal-title { display: block; font-size: 32rpx; font-weight: 600; color: #e2e8f0; margin-bottom: 8rpx; }
  .goal-desc { display: block; margin-bottom: 20rpx; line-height: 1.5; }
  .goal-progress { margin-bottom: 16rpx; }
  .progress-header { display: flex; justify-content: space-between; margin-bottom: 8rpx; }
  .progress-bar { height: 10rpx; background: rgba(255,255,255,0.08); border-radius: 5rpx; overflow: hidden; margin-bottom: 16rpx; }
  .progress-fill { height: 100%; border-radius: 5rpx; transition: width 0.3s; }
  .progress-controls { display: flex; gap: 12rpx; }
  .progress-btn { padding: 8rpx 16rpx; border-radius: 8rpx; font-size: 22rpx; color: #64748b; background: rgba(255,255,255,0.05);
    &.active { background: rgba(59,130,246,0.2); color: #60a5fa; }
  }
}

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80rpx 24rpx;
  .empty-icon { font-size: 96rpx; margin-bottom: 20rpx; }
  .empty-text { font-size: 28rpx; color: #64748b; text-align: center; }
}

.add-modal { padding: 40rpx;
  .modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 32rpx; }
  .modal-title { font-size: 36rpx; font-weight: 700; color: #e2e8f0; }
  .modal-close { font-size: 36rpx; color: #64748b; }
  .form-item { margin-bottom: 24rpx; }
  .form-label { display: block; font-size: 26rpx; color: #94a3b8; margin-bottom: 12rpx; }
  .form-input { width: 100%; background: #252836; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 28rpx; color: #e2e8f0; box-sizing: border-box; }
  .form-textarea { width: 100%; background: #252836; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 28rpx; color: #e2e8f0; box-sizing: border-box; min-height: 100rpx; }
  .goal-types { display: flex; flex-wrap: wrap; gap: 12rpx; }
  .type-chip { padding: 12rpx 28rpx; border-radius: 100rpx; font-size: 26rpx; color: #64748b; background: rgba(255,255,255,0.05);
    &.active { background: rgba(59,130,246,0.2); color: #60a5fa; }
  }
  .form-row { display: flex; gap: 20rpx; margin-bottom: 24rpx; }
  .half-item { flex: 1; }
  .picker-btn { background: #252836; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 26rpx; color: #e2e8f0; }
  .submit-btn { width: 100%; height: 96rpx; background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: white; border-radius: 20rpx; font-size: 32rpx; font-weight: 600; border: none; margin-top: 8rpx; }
}
</style>
