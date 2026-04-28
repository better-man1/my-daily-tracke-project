<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-title">每日摘录</text>
      <view class="add-btn" @tap="openAdd">＋</view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <text class="search-icon">🔍</text>
      <input v-model="keyword" class="search-input" placeholder="搜索摘录内容..." placeholder-style="color:#475569" @confirm="doSearch" />
    </view>

    <!-- 列表 -->
    <view class="excerpt-list">
      <view v-for="item in list" :key="item.id" class="excerpt-card card">
        <view class="excerpt-header">
          <text class="source-tag">{{ sourceLabel(item.sourceType) }}</text>
          <view class="excerpt-actions">
            <text class="fav-btn" :style="{ opacity: item.isFavorite ? '1' : '0.3' }" @tap="toggleFav(item)">⭐</text>
            <text class="del-btn" @tap="deleteItem(item.id)">✕</text>
          </view>
        </view>
        <text class="excerpt-content">"{{ item.content }}"</text>
        <text v-if="item.sourceTitle" class="excerpt-source">📌 {{ item.sourceTitle }}</text>
        <text v-if="item.thought" class="excerpt-thought">💭 {{ item.thought }}</text>
        <text class="excerpt-date text-muted text-xs">{{ item.excerptDate }}</text>
      </view>

      <view v-if="!list.length" class="empty-state">
        <text class="empty-icon">📖</text>
        <text class="empty-text">还没有摘录，记录你的第一条知识</text>
      </view>
    </view>

    <!-- 新增弹窗 -->
    <uni-popup ref="popup" type="bottom" background-color="#1a1d27">
      <view v-if="showForm" class="add-modal">
        <view class="modal-header">
          <text class="modal-title">新增摘录</text>
          <text class="modal-close" @tap="showForm = false">✕</text>
        </view>

        <view class="form-item">
          <text class="form-label">摘录内容 *</text>
          <textarea v-model="form.content" class="form-textarea" placeholder="输入摘录内容..." placeholder-style="color:#475569" auto-height />
        </view>

        <view class="form-item">
          <text class="form-label">来源</text>
          <input v-model="form.sourceTitle" class="form-input" placeholder="书名/文章/视频标题（可选）" placeholder-style="color:#475569" />
        </view>

        <view class="form-item">
          <text class="form-label">个人感悟</text>
          <textarea v-model="form.thought" class="form-textarea" placeholder="写下你的感悟..." placeholder-style="color:#475569" />
        </view>

        <view class="form-item">
          <text class="form-label">来源类型</text>
          <view class="source-types">
            <view v-for="st in sourceTypes" :key="st.value" class="source-chip" :class="{ active: form.sourceType === st.value }" @tap="form.sourceType = st.value">
              {{ st.label }}
            </view>
          </view>
        </view>

        <button class="submit-btn" :loading="saving" @tap="save">保存摘录</button>
      </view>
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { excerptApi } from '../../api/index'

const list = ref<any[]>([])
const keyword = ref('')
const showForm = ref(false)
const saving = ref(false)
const popup = ref<any>()

const sourceTypes = [
  { value: 'BOOK', label: '📚 书籍' },
  { value: 'ARTICLE', label: '📰 文章' },
  { value: 'VIDEO', label: '🎬 视频' },
  { value: 'OTHER', label: '📌 其他' }
]

const form = reactive({
  content: '',
  sourceTitle: '',
  thought: '',
  sourceType: 'BOOK',
  excerptDate: new Date().toISOString().split('T')[0]
})

function sourceLabel(v: string) { return sourceTypes.find(s => s.value === v)?.label ?? v }

async function loadList() {
  const res = await excerptApi.page({ pageNum: 1, pageSize: 20 })
  list.value = res.records ?? []
}

async function doSearch() {
  if (!keyword.value.trim()) { loadList(); return }
  const res = await excerptApi.page({ pageNum: 1, pageSize: 20, keyword: keyword.value })
  list.value = res.records ?? []
}

async function toggleFav(item: any) {
  await excerptApi.toggleFavorite(item.id)
  item.isFavorite = item.isFavorite ? 0 : 1
}

async function deleteItem(id: number) {
  const r = await uni.showModal({ title: '提示', content: '确认删除？', confirmText: '删除', confirmColor: '#ef4444' })
  if (r.confirm) { await excerptApi.delete(id); loadList() }
}

function openAdd() { form.content = ''; form.sourceTitle = ''; form.thought = ''; form.sourceType = 'BOOK'; showForm.value = true }

async function save() {
  if (!form.content.trim()) { uni.showToast({ title: '请输入摘录内容', icon: 'none' }); return }
  saving.value = true
  try {
    await excerptApi.create({ ...form })
    uni.showToast({ title: '保存成功', icon: 'success' })
    showForm.value = false
    loadList()
  } finally { saving.value = false }
}

watch(showForm, (v) => { v ? popup.value?.open() : popup.value?.close() })
onMounted(loadList)
</script>

<style lang="scss" scoped>
.page-container { background: #0f1117; min-height: 100vh; padding: 24rpx; padding-bottom: 40rpx; }

.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24rpx;
  .page-title { font-size: 44rpx; font-weight: 700; color: #e2e8f0; }
  .add-btn { width: 72rpx; height: 72rpx; border-radius: 50%; background: #f59e0b; color: white; display: flex; align-items: center; justify-content: center; font-size: 40rpx; }
}

.search-bar { display: flex; align-items: center; gap: 16rpx; background: #1a1d27; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 20rpx; padding: 16rpx 24rpx; margin-bottom: 24rpx;
  .search-icon { font-size: 36rpx; }
  .search-input { flex: 1; font-size: 28rpx; color: #e2e8f0; }
}

.excerpt-list { display: flex; flex-direction: column; gap: 16rpx; }

.excerpt-card { padding: 28rpx;
  .excerpt-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16rpx; }
  .source-tag { font-size: 22rpx; color: #64748b; background: rgba(255,255,255,0.06); padding: 4rpx 16rpx; border-radius: 100rpx; }
  .excerpt-actions { display: flex; gap: 16rpx; }
  .fav-btn { font-size: 36rpx; }
  .del-btn { font-size: 28rpx; color: #475569; }
  .excerpt-content { display: block; font-size: 28rpx; color: #e2e8f0; line-height: 1.7; font-style: italic; margin-bottom: 12rpx; }
  .excerpt-source { display: block; font-size: 24rpx; color: #94a3b8; margin-bottom: 8rpx; }
  .excerpt-thought { display: block; font-size: 26rpx; color: #94a3b8; background: rgba(255,255,255,0.04); border-left: 4rpx solid #6366f1; padding: 12rpx 16rpx; border-radius: 0 8rpx 8rpx 0; margin-bottom: 12rpx; }
  .excerpt-date { display: block; }
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
  .form-textarea { width: 100%; background: #252836; border: 1rpx solid rgba(255,255,255,0.08); border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 28rpx; color: #e2e8f0; box-sizing: border-box; min-height: 120rpx; }
  .source-types { display: flex; flex-wrap: wrap; gap: 16rpx; }
  .source-chip { padding: 12rpx 24rpx; border-radius: 100rpx; font-size: 26rpx; color: #64748b; background: rgba(255,255,255,0.05);
    &.active { background: rgba(245,158,11,0.2); color: #f59e0b; }
  }
  .submit-btn { width: 100%; height: 96rpx; background: linear-gradient(135deg, #f59e0b, #d97706); color: white; border-radius: 20rpx; font-size: 32rpx; font-weight: 600; border: none; margin-top: 8rpx; }
}
</style>
