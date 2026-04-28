<template>
  <div class="summary-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">每日总结</h1>
        <p class="page-subtitle">坚持复盘，持续成长</p>
      </div>
      <el-button type="primary" size="small" :icon="Plus" @click="openToday">今日总结</el-button>
    </div>

    <!-- 连续打卡信息 -->
    <div class="streak-bar card">
      <div class="streak-item">
        <div class="streak-num">🔥 {{ streak.currentStreak ?? 0 }}</div>
        <div class="streak-label">当前连续天数</div>
      </div>
      <div class="streak-divider" />
      <div class="streak-item">
        <div class="streak-num">⚡ {{ streak.longestStreak ?? 0 }}</div>
        <div class="streak-label">历史最长</div>
      </div>
      <div class="streak-divider" />
      <div class="streak-item">
        <div class="streak-num">📅 {{ streak.totalDays ?? 0 }}</div>
        <div class="streak-label">累计总结天数</div>
      </div>
    </div>

    <!-- 总结列表 -->
    <div v-loading="loading" class="summary-list">
      <div v-for="item in list" :key="item.id" class="summary-card card card--glow">
        <div class="summary-header">
          <span class="summary-date">{{ item.summaryDate }}</span>
          <div class="summary-scores">
            <span class="mood">{{ moodEmoji[item.mood - 1] }}</span>
            <span class="score">{{ item.score }}/10</span>
          </div>
        </div>
        <div v-if="item.achievement" class="summary-section">
          <div class="section-title">✅ 今日成就</div>
          <div class="section-content">{{ item.achievement }}</div>
        </div>
        <div v-if="item.improvement" class="summary-section">
          <div class="section-title">📈 待改进</div>
          <div class="section-content">{{ item.improvement }}</div>
        </div>
        <div v-if="item.tomorrowPlan" class="summary-section">
          <div class="section-title">📋 明日计划</div>
          <div class="section-content">{{ item.tomorrowPlan }}</div>
        </div>
        <div class="summary-actions">
          <el-icon class="icon-btn" @click="editItem(item)"><Edit /></el-icon>
          <el-icon class="icon-btn danger" @click="deleteItem(item.id)"><Delete /></el-icon>
        </div>
      </div>

      <div v-if="!loading && list.length === 0" class="empty-state">
        <div class="empty-icon">✍️</div>
        <p>还没有总结记录</p>
        <el-button type="primary" size="small" @click="openToday">写今日总结</el-button>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="showDialog"
      :title="editing ? '编辑总结' : '今日总结'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-form-item label="总结日期">
          <el-date-picker
            v-model="form.summaryDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <div class="flex gap-md">
          <el-form-item label="今日心情" style="flex: 1">
            <el-rate
              v-model="form.mood"
              :max="5"
              :texts="moodEmoji"
              show-text
              style="height: 32px"
            />
          </el-form-item>
          <el-form-item label="今日评分" style="flex: 1">
            <el-input-number v-model="form.score" :min="1" :max="10" style="width: 120px" />
            <span class="text-muted text-sm" style="margin-left: 8px">/ 10</span>
          </el-form-item>
        </div>
        <el-form-item label="今日成就">
          <el-input
            v-model="form.achievement"
            type="textarea"
            :rows="2"
            placeholder="今天做到了什么值得骄傲的事？"
          />
        </el-form-item>
        <el-form-item label="待改进">
          <el-input
            v-model="form.improvement"
            type="textarea"
            :rows="2"
            placeholder="哪些方面可以做得更好？"
          />
        </el-form-item>
        <el-form-item label="明日计划">
          <el-input
            v-model="form.tomorrowPlan"
            type="textarea"
            :rows="2"
            placeholder="明天的主要任务是什么？"
          />
        </el-form-item>
        <el-form-item label="自由日记">
          <el-input
            v-model="form.freeWriting"
            type="textarea"
            :rows="3"
            placeholder="随意写写今天的感受..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { summaryApi } from '@/api/summary'
import type { SummaryItem } from '@/api/summary'
import dayjs from 'dayjs'

const list = ref<SummaryItem[]>([])
const streak = ref<Record<string, any>>({})
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const editing = ref<SummaryItem | null>(null)
const formRef = ref<FormInstance>()
const moodEmoji = ['😢', '😔', '😐', '😊', '😄']

const form = reactive({
  summaryDate: dayjs().format('YYYY-MM-DD'),
  mood: 3,
  score: 7,
  achievement: '',
  improvement: '',
  tomorrowPlan: '',
  freeWriting: ''
})

async function loadList() {
  loading.value = true
  try {
    list.value = await summaryApi.list({ pageNum: 1, pageSize: 20 })
  } finally {
    loading.value = false
  }
}

async function loadStreak() {
  try {
    streak.value = (await summaryApi.getStreak()) as any
  } catch {}
}

function openToday() {
  editing.value = null
  Object.assign(form, {
    summaryDate: dayjs().format('YYYY-MM-DD'),
    mood: 3,
    score: 7,
    achievement: '',
    improvement: '',
    tomorrowPlan: '',
    freeWriting: ''
  })
  showDialog.value = true
}

function editItem(item: SummaryItem) {
  editing.value = item
  Object.assign(form, {
    summaryDate: item.summaryDate,
    mood: item.mood ?? 3,
    score: item.score ?? 7,
    achievement: item.achievement ?? '',
    improvement: item.improvement ?? '',
    tomorrowPlan: item.tomorrowPlan ?? '',
    freeWriting: item.freeWriting ?? ''
  })
  showDialog.value = true
}

async function deleteItem(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await summaryApi.delete(id)
  ElMessage.success('已删除')
  loadList()
}

async function save() {
  saving.value = true
  try {
    if (editing.value) {
      await summaryApi.update(editing.value.id, form as any)
    } else {
      await summaryApi.create(form as any)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    await Promise.all([loadList(), loadStreak()])
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadList()
  loadStreak()
})
</script>

<style lang="scss" scoped>
.summary-view {
  .streak-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0;
    padding: 20px;
    margin-bottom: 20px;

    .streak-item {
      flex: 1;
      text-align: center;

      .streak-num {
        font-size: 24px;
        font-weight: 700;
        color: $text-primary;
      }
      .streak-label {
        font-size: 12px;
        color: $text-muted;
        margin-top: 4px;
      }
    }

    .streak-divider {
      width: 1px;
      height: 40px;
      background: $border;
      margin: 0 24px;
    }
  }

  .summary-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .summary-card {
    position: relative;

    .summary-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;

      .summary-date {
        font-size: 15px;
        font-weight: 600;
      }
      .summary-scores {
        display: flex;
        align-items: center;
        gap: 10px;

        .mood {
          font-size: 20px;
        }
        .score {
          font-size: 16px;
          font-weight: 700;
          color: $primary-light;
        }
      }
    }

    .summary-section {
      margin-bottom: 8px;

      .section-title {
        font-size: 12px;
        font-weight: 600;
        color: $text-secondary;
        margin-bottom: 4px;
      }
      .section-content {
        font-size: 14px;
        color: $text-primary;
        line-height: 1.6;
      }
    }

    .summary-actions {
      position: absolute;
      top: 16px;
      right: 16px;
      display: flex;
      gap: 6px;
      opacity: 0;
      transition: $transition-fast;

      .icon-btn {
        color: $text-muted;
        cursor: pointer;
        padding: 4px;
        border-radius: $radius-sm;
        font-size: 15px;
        transition: $transition-fast;

        &:hover {
          color: $primary-light;
          background: $primary-100;
        }
        &.danger:hover {
          color: $danger;
          background: rgba($danger, 0.1);
        }
      }
    }

    &:hover .summary-actions {
      opacity: 1;
    }
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
    color: $text-muted;
    .empty-icon {
      font-size: 48px;
      margin-bottom: 12px;
    }
    p {
      margin-bottom: 16px;
    }
  }
}
</style>
