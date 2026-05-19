<template>
  <view class="page-container">
    <dt-navbar :title="isEdit ? '编辑计划' : '新建计划'" show-back />

    <view class="page-content">
      <view class="form-card fade-in">
        <!-- 标题 -->
        <view class="form-group">
          <text class="form-label">任务标题 <text class="required">*</text></text>
          <input
            class="form-input"
            v-model="form.title"
            placeholder="输入计划标题..."
            placeholder-class="input-placeholder"
            maxlength="100"
          />
        </view>

        <!-- 描述 -->
        <view class="form-group">
          <text class="form-label">描述（可选）</text>
          <textarea
            class="form-textarea"
            v-model="form.description"
            placeholder="详细描述..."
            placeholder-class="input-placeholder"
            maxlength="500"
            :auto-height="true"
          />
        </view>

        <!-- 优先级 -->
        <view class="form-group">
          <text class="form-label">优先级</text>
          <view class="radio-group">
            <view v-for="p in priorities" :key="p.value"
              class="radio-item"
              :class="{ 'radio-item--active': form.priority === p.value, [`radio-item--${p.color}`]: form.priority === p.value }"
              @tap="form.priority = p.value">
              <text class="radio-text">{{ p.label }}</text>
            </view>
          </view>
        </view>

        <!-- 分类 -->
        <view class="form-group">
          <text class="form-label">任务分类</text>
          <view class="radio-group">
            <view v-for="c in categories" :key="c.value"
              class="radio-item"
              :class="{ 'radio-item--active': form.category === c.value }"
              @tap="form.category = c.value">
              <text class="radio-text">{{ c.icon }} {{ c.label }}</text>
            </view>
          </view>
        </view>

        <!-- 计划日期 -->
        <view class="form-group">
          <text class="form-label">计划日期</text>
          <picker mode="date" :value="form.planDate" @change="form.planDate = $event.detail.value">
            <view class="picker-field">
              <text class="picker-text">{{ form.planDate }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <!-- 预估时间 -->
        <view class="form-group">
          <text class="form-label">预估时长（分钟）</text>
          <input
            class="form-input"
            v-model="form.estimatedMins"
            type="number"
            placeholder="如：30"
            placeholder-class="input-placeholder"
          />
        </view>

        <!-- 时间块 -->
        <view class="form-group">
          <text class="form-label">时间块（可选）</text>
          <view class="time-range">
            <picker mode="time" :value="form.startTime || ''" @change="form.startTime = $event.detail.value">
              <view class="time-picker">
                <text class="time-text">{{ form.startTime || '开始时间' }}</text>
              </view>
            </picker>
            <text class="time-sep">—</text>
            <picker mode="time" :value="form.endTime || ''" @change="form.endTime = $event.detail.value">
              <view class="time-picker">
                <text class="time-text">{{ form.endTime || '结束时间' }}</text>
              </view>
            </picker>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="action-btns">
        <button class="btn-cancel" @tap="uni.navigateBack()">
          <text>取消</text>
        </button>
        <button class="btn-submit" :loading="loading" @tap="handleSubmit">
          <text>{{ isEdit ? '保 存' : '创 建' }}</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { planApi, type PlanItem } from '@/api/plan'
import { getToday } from '@/utils/date'

const loading = ref(false)
const planId = ref<number | null>(null)
const isEdit = computed(() => !!planId.value)

const form = ref({
  title: '',
  description: '',
  planDate: getToday(),
  priority: 'P2',
  category: 'WORK',
  estimatedMins: '',
  startTime: '',
  endTime: ''
})

const priorities = [
  { label: '🔴 P0 紧急', value: 'P0', color: 'danger' },
  { label: '🟡 P1 重要', value: 'P1', color: 'warning' },
  { label: '🔵 P2 普通', value: 'P2', color: 'primary' },
  { label: '⚪ P3 低优', value: 'P3', color: 'info' }
]

const categories = [
  { icon: '💼', label: '工作', value: 'WORK' },
  { icon: '📚', label: '学习', value: 'STUDY' },
  { icon: '🌿', label: '生活', value: 'LIFE' },
  { icon: '🏃', label: '健康', value: 'HEALTH' }
]

onLoad((options: any) => {
  if (options?.date) {
    form.value.planDate = options.date
  }
  if (options?.id) {
    planId.value = Number(options.id)
    // 这里可以加载现有计划数据进行编辑
  }
})

async function handleSubmit() {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }

  loading.value = true
  try {
    const data: any = {
      title: form.value.title.trim(),
      description: form.value.description || undefined,
      planDate: form.value.planDate,
      priority: form.value.priority,
      category: form.value.category,
      estimatedMins: form.value.estimatedMins ? Number(form.value.estimatedMins) : undefined,
      startTime: form.value.startTime || undefined,
      endTime: form.value.endTime || undefined,
      isTimeblock: (form.value.startTime && form.value.endTime) ? 1 : 0
    }

    if (isEdit.value && planId.value) {
      await planApi.update(planId.value, data)
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await planApi.create(data)
      uni.showToast({ title: '创建成功', icon: 'success' })
    }

    setTimeout(() => uni.navigateBack(), 800)
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.form-card {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 32rpx;
  margin-bottom: $dt-space-lg;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.form-group {
  margin-bottom: 32rpx;

  &:last-child { margin-bottom: 0; }
}

.form-label {
  font-size: $dt-font-sm;
  color: $dt-text-secondary;
  font-weight: 500;
  display: block;
  margin-bottom: 12rpx;

  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }

  .required { color: $dt-danger; }
}

.form-input {
  width: 100%;
  height: 88rpx;
  background: rgba(0, 0, 0, 0.03);
  border-radius: $dt-radius-md;
  padding: 0 24rpx;
  font-size: $dt-font-md;
  color: $dt-text-primary;
  box-sizing: border-box;

  @media (prefers-color-scheme: dark) {
    background: rgba(255, 255, 255, 0.06);
    color: $dt-dark-text-primary;
  }
}

.form-textarea {
  width: 100%;
  min-height: 120rpx;
  background: rgba(0, 0, 0, 0.03);
  border-radius: $dt-radius-md;
  padding: 20rpx 24rpx;
  font-size: $dt-font-md;
  color: $dt-text-primary;
  box-sizing: border-box;

  @media (prefers-color-scheme: dark) {
    background: rgba(255, 255, 255, 0.06);
    color: $dt-dark-text-primary;
  }
}

.input-placeholder {
  color: $dt-text-placeholder;
  font-size: $dt-font-md;
}

// 单选组
.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.radio-item {
  padding: 12rpx 24rpx;
  border-radius: $dt-radius-full;
  background: rgba(0, 0, 0, 0.04);
  border: 2rpx solid transparent;
  transition: all 0.2s;

  @media (prefers-color-scheme: dark) { background: rgba(255, 255, 255, 0.06); }

  &--active {
    border-color: $dt-primary;
    background: $dt-primary-bg;
  }

  &--active.radio-item--danger { border-color: $dt-danger; background: rgba(239, 68, 68, 0.1); }
  &--active.radio-item--warning { border-color: $dt-warning; background: rgba(245, 158, 11, 0.1); }
  &--active.radio-item--info { border-color: $dt-info; background: rgba(107, 114, 128, 0.1); }

  .radio-text {
    font-size: $dt-font-sm;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &--active .radio-text { color: $dt-primary; font-weight: 500; }
  &--active.radio-item--danger .radio-text { color: $dt-danger; }
  &--active.radio-item--warning .radio-text { color: $dt-warning; }
  &--active.radio-item--info .radio-text { color: $dt-info; }
}

// 日期选择器
.picker-field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  background: rgba(0, 0, 0, 0.03);
  border-radius: $dt-radius-md;
  padding: 0 24rpx;

  @media (prefers-color-scheme: dark) { background: rgba(255, 255, 255, 0.06); }

  .picker-text {
    font-size: $dt-font-md;
    color: $dt-text-primary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
  }

  .picker-arrow {
    font-size: 40rpx;
    color: $dt-text-secondary;
    font-weight: 300;
  }
}

// 时间范围
.time-range {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.time-picker {
  flex: 1;
  height: 88rpx;
  background: rgba(0, 0, 0, 0.03);
  border-radius: $dt-radius-md;
  display: flex;
  align-items: center;
  justify-content: center;

  @media (prefers-color-scheme: dark) { background: rgba(255, 255, 255, 0.06); }

  .time-text {
    font-size: $dt-font-md;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }
}

.time-sep {
  font-size: $dt-font-md;
  color: $dt-text-secondary;
}

// 操作按钮
.action-btns {
  display: flex;
  gap: 20rpx;
  padding-bottom: 60rpx;
}

.btn-cancel {
  flex: 1;
  height: 96rpx;
  background: rgba(0, 0, 0, 0.04);
  border-radius: $dt-radius-md;
  border: none;
  font-size: $dt-font-md;
  color: $dt-text-secondary;

  @media (prefers-color-scheme: dark) {
    background: rgba(255, 255, 255, 0.08);
    color: $dt-dark-text-secondary;
  }

  &::after { border: none; }
}

.btn-submit {
  flex: 2;
  height: 96rpx;
  background: linear-gradient(135deg, $dt-primary, $dt-primary-light);
  border-radius: $dt-radius-md;
  border: none;
  font-size: $dt-font-lg;
  color: #FFFFFF;
  font-weight: 700;
  letter-spacing: 4rpx;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.3);

  &::after { border: none; }
  &:active { opacity: 0.85; }
}
</style>
