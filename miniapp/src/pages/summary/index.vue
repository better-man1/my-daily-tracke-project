<template>
  <view class="page-container">
    <view class="page-header">
      <view>
        <text class="page-title">每日总结</text>
        <text class="page-subtitle">坚持复盘，持续成长</text>
      </view>
      <view class="add-btn" @tap="openTodayForm">{{ todaySummary ? '编辑' : '今日' }}</view>
    </view>

    <!-- 连续打卡 -->
    <view class="streak-row">
      <view class="streak-card">
        <text class="streak-num">🔥 {{ streak.currentStreak ?? 0 }}</text>
        <text class="streak-label">当前连续</text>
      </view>
      <view class="streak-card">
        <text class="streak-num">⚡ {{ streak.longestStreak ?? 0 }}</text>
        <text class="streak-label">历史最长</text>
      </view>
      <view class="streak-card">
        <text class="streak-num">📅 {{ streak.totalDays ?? 0 }}</text>
        <text class="streak-label">累计天数</text>
      </view>
    </view>

    <!-- 总结列表 -->
    <view class="summary-list">
      <view v-for="item in list" :key="item.id" class="summary-card card">
        <view class="summary-header">
          <text class="summary-date">{{ item.summaryDate }}</text>
          <view class="summary-scores">
            <text class="mood-emoji">{{ moodEmoji[item.mood - 1] }}</text>
            <text class="score-text">{{ item.score }}/10</text>
          </view>
        </view>
        <view v-if="item.achievement" class="summary-section">
          <text class="section-title">✅ 今日成就</text>
          <text class="section-content">{{ item.achievement }}</text>
        </view>
        <view v-if="item.improvement" class="summary-section">
          <text class="section-title">📈 待改进</text>
          <text class="section-content">{{ item.improvement }}</text>
        </view>
        <view v-if="item.tomorrowPlan" class="summary-section">
          <text class="section-title">📋 明日计划</text>
          <text class="section-content">{{ item.tomorrowPlan }}</text>
        </view>
      </view>

      <view v-if="!list.length" class="empty-state">
        <text class="empty-icon">✍️</text>
        <text class="empty-text">还没有总结记录</text>
      </view>
    </view>

    <!-- 今日总结弹窗 -->
    <uni-popup ref="popup" type="bottom" background-color="#1a1d27">
      <view v-if="showForm" class="add-modal">
        <view class="modal-header">
          <text class="modal-title">{{ todaySummary ? '编辑总结' : '今日总结' }}</text>
          <text class="modal-close" @tap="showForm = false">✕</text>
        </view>

        <!-- 心情选择 -->
        <view class="mood-row">
          <text class="form-label">今日心情</text>
          <view class="mood-options">
            <text
              v-for="(emoji, i) in moodEmoji"
              :key="i"
              class="mood-option"
              :class="{ selected: form.mood === i + 1 }"
              @tap="form.mood = i + 1"
            >{{ emoji }}</text>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">今日评分 ({{ form.score }}/10)</text>
          <slider :value="form.score" :min="1" :max="10" :step="1" @change="e => form.score = e.detail.value" active-color="#6366f1" />
        </view>

        <view class="form-item">
          <text class="form-label">今日成就</text>
          <textarea v-model="form.achievement" class="form-textarea" placeholder="今天做了什么值得骄傲的事？" placeholder-style="color:#475569" />
        </view>

        <view class="form-item">
          <text class="form-label">待改进</text>
          <textarea v-model="form.improvement" class="form-textarea" placeholder="哪些方面可以做得更好？" placeholder-style="color:#475569" />
        </view>

        <view class="form-item">
          <text class="form-label">明日计划</text>
          <textarea v-model="form.tomorrowPlan" class="form-textarea" placeholder="明天的主要任务是什么？" placeholder-style="color:#475569" />
        </view>

        <button class="submit-btn" :loading="saving" @tap="saveSummary">保存总结</button>
      </view>
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { summaryApi } from '../../api/index'

const list = ref<any[]>([])
const streak = ref<any>({})
const todaySummary = ref<any>(null)
const showForm = ref(false)
const saving = ref(false)
const popup = ref<any>()

const moodEmoji = ['😢', '😔', '😐', '😊', '😄']
const today = new Date().toISOString().split('T')[0]

const form = reactive({
  summaryDate: today,
  mood: 3,
  score: 7,
  achievement: '',
  improvement: '',
  tomorrowPlan: '',
  freeWriting: ''
})

function openTodayForm() {
  if (todaySummary.value) {
    Object.assign(form, {
      summaryDate: todaySummary.value.summaryDate,
      mood: todaySummary.value.mood ?? 3,
      score: todaySummary.value.score ?? 7,
      achievement: todaySummary.value.achievement ?? '',
      improvement: todaySummary.value.improvement ?? '',
      tomorrowPlan: todaySummary.value.tomorrowPlan ?? '',
    })
  } else {
    form.mood = 3; form.score = 7
    form.achievement = ''; form.improvement = ''; form.tomorrowPlan = ''
    form.summaryDate = today
  }
  showForm.value = true
}

async function loadData() {
  try {
    list.value = await summaryApi.list({ pageNum: 1, pageSize: 20 })
    streak.value = await summaryApi.getStreak()
    todaySummary.value = await summaryApi.getToday()
  } catch (e) { console.error(e) }
}

async function saveSummary() {
  saving.value = true
  try {
    if (todaySummary.value) {
      await summaryApi.update(todaySummary.value.id, form as any)
    } else {
      await summaryApi.create(form as any)
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    showForm.value = false
    await loadData()
  } finally { saving.value = false }
}

watch(showForm, (v) => { v ? popup.value?.open() : popup.value?.close() })

onMounted(loadData)
onShow(loadData)
</script>

<style lang="scss" scoped>
.page-container { background: #0f1117; min-height: 100vh; padding: 24rpx; padding-bottom: 120rpx; }

.page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 24rpx;
  .page-title { font-size: 44rpx; font-weight: 700; color: #e2e8f0; display: block; }
  .page-subtitle { font-size: 24rpx; color: #94a3b8; }
  .add-btn { background: rgba(99,102,241,0.2); color: #818cf8; border: 1rpx solid rgba(99,102,241,0.3); border-radius: 16rpx; padding: 12rpx 28rpx; font-size: 26rpx; }
}

.streak-row { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12rpx; margin-bottom: 24rpx;
  .streak-card { background: #1a1d27; border-radius: 20rpx; padding: 24rpx; text-align: center; border: 1rpx solid rgba(255,255,255,0.08);
    .streak-num { font-size: 32rpx; font-weight: 700; color: #e2e8f0; display: block; }
    .streak-label { font-size: 22rpx; color: #64748b; margin-top: 8rpx; display: block; }
  }
}

.summary-list { display: flex; flex-direction: column; gap: 16rpx; }

.summary-card { padding: 28rpx;
  .summary-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20rpx; }
  .summary-date { font-size: 30rpx; font-weight: 600; color: #e2e8f0; }
  .summary-scores { display: flex; align-items: center; gap: 16rpx; }
  .mood-emoji { font-size: 40rpx; }
  .score-text { font-size: 28rpx; font-weight: 700; color: #818cf8; }
  .summary-section { margin-bottom: 16rpx; }
  .section-title { display: block; font-size: 24rpx; color: #94a3b8; margin-bottom: 8rpx; }
  .section-content { font-size: 28rpx; color: #cbd5e1; line-height: 1.6; }
}

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80rpx 24rpx;
  .empty-icon { font-size: 96rpx; margin-bottom: 20rpx; }
  .empty-text { font-size: 28rpx; color: #64748b; }
}

.add-modal { padding: 40rpx; max-height: 80vh; overflow-y: auto;
  .modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 32rpx; }
  .modal-title { font-size: 36rpx; font-weight: 700; color: #e2e8f0; }
  .modal-close { font-size: 36rpx; color: #64748b; }
  .form-label { display: block; font-size: 26rpx; color: #94a3b8; margin-bottom: 12rpx; }
  .form-item { margin-bottom: 24rpx; }
  .form-textarea { width: 100%; background: #252836; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 28rpx; color: #e2e8f0; box-sizing: border-box; height: 120rpx; }
  .mood-row { margin-bottom: 24rpx; }
  .mood-options { display: flex; gap: 20rpx; }
  .mood-option { font-size: 60rpx; opacity: 0.4; transition: transform 0.1s;
    &.selected { opacity: 1; transform: scale(1.2); }
  }
  .submit-btn { width: 100%; height: 96rpx; background: linear-gradient(135deg, #6366f1, #7c3aed); color: white; border-radius: 20rpx; font-size: 32rpx; font-weight: 600; border: none; margin-top: 16rpx; }
}
</style>
