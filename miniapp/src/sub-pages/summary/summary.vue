<!--
/**
 * ============================================================================
 * summary.vue — 每日总结页面
 * ============================================================================
 *
 * 【页面说明】填写今日心情/评分/成就/改进/明日计划/感恩，支持编辑已有总结
 * 【路由路径】/sub-pages/summary/summary
 * 【页面传参】无
 * 【关键 API】uni.navigateBack / uni.showToast / onShow
 * ============================================================================
 */
-->

<template>
  <view class="page-container">
    <dt-navbar title="每日总结" show-back />

    <view class="page-content">
      <!-- 日期与连续打卡 -->
      <view class="header-card fade-in">
        <view class="header-date">
          <text class="date-emoji">📔</text>
          <view class="date-info">
            <text class="date-text">{{ today }}</text>
            <text class="streak-text" v-if="streak > 0">🔥 已连续打卡 {{ streak }} 天</text>
          </view>
        </view>
      </view>

      <!-- 心情与评分 -->
      <view class="form-card slide-up">
        <text class="section-title">今日心情</text>
        <view class="mood-selector">
          <view v-for="m in moods" :key="m.value"
            class="mood-item"
            :class="{ 'mood-item--active': form.mood === m.value }"
            @tap="form.mood = m.value">
            <text class="mood-emoji">{{ m.emoji }}</text>
            <text class="mood-label">{{ m.label }}</text>
          </view>
        </view>

        <text class="section-title mt-md">综合评分</text>
        <view class="score-stars">
          <view v-for="s in 10" :key="s"
            class="star-item"
            @tap="form.score = s">
            <text class="star-icon" :class="{ 'star-icon--active': s <= form.score }">★</text>
          </view>
        </view>
        <text class="score-text">{{ form.score }}/10</text>
      </view>

      <!-- 今日成就 -->
      <view class="form-card slide-up">
        <text class="section-title">🏆 今日成就</text>
        <textarea
          class="form-textarea"
          v-model="form.achievement"
          placeholder="今天完成了哪些重要的事情？"
          placeholder-class="textarea-placeholder"
          maxlength="500"
          :auto-height="true"
        />
      </view>

      <!-- 待改进 -->
      <view class="form-card slide-up">
        <text class="section-title">💡 待改进</text>
        <textarea
          class="form-textarea"
          v-model="form.improvement"
          placeholder="哪些地方做得不够好，下次如何改进？"
          placeholder-class="textarea-placeholder"
          maxlength="500"
          :auto-height="true"
        />
      </view>

      <!-- 明日计划 -->
      <view class="form-card slide-up">
        <text class="section-title">🚀 明日计划</text>
        <textarea
          class="form-textarea"
          v-model="form.tomorrowPlan"
          placeholder="明天最重要的3件事是什么？"
          placeholder-class="textarea-placeholder"
          maxlength="500"
          :auto-height="true"
        />
      </view>

      <!-- 感恩 -->
      <view class="form-card slide-up">
        <text class="section-title">🙏 今日感恩</text>
        <textarea
          class="form-textarea"
          v-model="form.gratitude"
          placeholder="今天有什么值得感谢的人或事？"
          placeholder-class="textarea-placeholder"
          maxlength="300"
          :auto-height="true"
        />
      </view>

      <!-- 随记 -->
      <view class="form-card slide-up">
        <text class="section-title">✍️ 随心记</text>
        <textarea
          class="form-textarea"
          v-model="form.freeWriting"
          placeholder="还有什么想说的？"
          placeholder-class="textarea-placeholder"
          maxlength="1000"
          :auto-height="true"
        />
      </view>

      <!-- 提交 -->
      <button class="submit-btn" :loading="loading" @tap="handleSubmit">
        <text class="submit-text">{{ isEdit ? '更 新' : '完成今日总结 ✨' }}</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
// Vue 3 Composition API
import { ref } from 'vue'
// Uni-app 生命周期
import { onShow } from '@dcloudio/uni-app'
// 总结 API — 打卡/今日总结/创建/更新
import { summaryApi, type SummaryItem } from '@/api/summary'
// 日期工具
import { getToday } from '@/utils/date'

const today = getToday()
const loading = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const streak = ref(0)

const form = ref({
  mood: 7,
  score: 7,
  achievement: '',
  improvement: '',
  tomorrowPlan: '',
  gratitude: '',
  freeWriting: ''
})

const moods = [
  { emoji: '😔', label: '很差', value: 2 },
  { emoji: '😐', label: '一般', value: 4 },
  { emoji: '🙂', label: '还好', value: 6 },
  { emoji: '😄', label: '不错', value: 8 },
  { emoji: '🥳', label: '超棒', value: 10 }
]


// ============================================================================
// 数据加载
// ============================================================================
async function loadData() {
  try {
    const [todaySummary, streakData] = await Promise.all([
      summaryApi.getToday().catch(() => null),
      summaryApi.getStreak().catch(() => ({ currentStreak: 0, longestStreak: 0, totalDays: 0 }))
    ])

    streak.value = streakData?.currentStreak || 0

    if (todaySummary) {
      isEdit.value = true
      editId.value = todaySummary.id
      form.value = {
        mood: todaySummary.mood || 7,
        score: todaySummary.score || 7,
        achievement: todaySummary.achievement || '',
        improvement: todaySummary.improvement || '',
        tomorrowPlan: todaySummary.tomorrowPlan || '',
        gratitude: todaySummary.gratitude || '',
        freeWriting: todaySummary.freeWriting || ''
      }
    }
  } catch (e) {
    console.error('Load summary error:', e)
  }
}


// ============================================================================
// 表单提交
// ============================================================================
async function handleSubmit() {
  loading.value = true
  try {
    const data = {
      summaryDate: today,
      mood: form.value.mood,
      score: form.value.score,
      achievement: form.value.achievement || undefined,
      improvement: form.value.improvement || undefined,
      tomorrowPlan: form.value.tomorrowPlan || undefined,
      gratitude: form.value.gratitude ? [form.value.gratitude] : undefined,
      freeWriting: form.value.freeWriting || undefined
    }

    if (isEdit.value && editId.value) {
      await summaryApi.update(editId.value, data)
    } else {
      await summaryApi.create(data)
    }

    uni.showToast({ title: '总结已保存 🎉', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 800)
  } catch {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}


// ============================================================================
// 生命周期
// ============================================================================
onShow(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
// 头部
.header-card {
  background: linear-gradient(135deg, #6366F1, #818CF8);
  border-radius: $dt-radius-xl;
  padding: 28rpx 32rpx;
  margin-bottom: $dt-space-lg;
}

.header-date {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.date-emoji { font-size: 56rpx; }

.date-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.date-text {
  font-size: $dt-font-xl;
  font-weight: 700;
  color: #FFFFFF;
}

.streak-text {
  font-size: $dt-font-sm;
  color: rgba(255, 255, 255, 0.8);
}

// 表单卡片
.form-card {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 32rpx;
  margin-bottom: $dt-space-lg;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.section-title {
  font-size: $dt-font-md;
  font-weight: 600;
  color: $dt-text-primary;
  display: block;
  margin-bottom: 24rpx;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.mt-md { margin-top: 32rpx; }

// 心情选择
.mood-selector {
  display: flex;
  justify-content: space-around;
  margin-bottom: 8rpx;
}

.mood-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 12rpx;
  border-radius: $dt-radius-md;
  transition: all 0.2s;

  &--active {
    background: $dt-primary-bg;
    transform: scale(1.1);
  }
}

.mood-emoji {
  font-size: 56rpx;
  transition: transform 0.2s;
}

.mood-label {
  font-size: $dt-font-xs;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.mood-item--active .mood-label {
  color: $dt-primary;
  font-weight: 500;
}

// 评分星星
.score-stars {
  display: flex;
  gap: 8rpx;
  margin-bottom: 8rpx;
}

.star-item {
  flex: 1;
  display: flex;
  justify-content: center;
}

.star-icon {
  font-size: 44rpx;
  color: $dt-divider;
  transition: color 0.2s;

  &--active {
    color: $dt-warning;
  }
}

.score-text {
  font-size: $dt-font-sm;
  color: $dt-text-secondary;
  text-align: center;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

// 文本区
.form-textarea {
  width: 100%;
  min-height: 120rpx;
  font-size: $dt-font-md;
  color: $dt-text-primary;
  line-height: 1.8;
  background: transparent;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.textarea-placeholder {
  color: $dt-text-placeholder;
  font-size: $dt-font-md;
}

// 提交
.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, $dt-primary, $dt-primary-light);
  border-radius: $dt-radius-md;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.3);
  margin-bottom: 60rpx;

  &::after { border: none; }
  &:active { opacity: 0.85; }

  .submit-text {
    color: #FFFFFF;
    font-size: $dt-font-md;
    font-weight: 700;
    letter-spacing: 2rpx;
  }
}
</style>
