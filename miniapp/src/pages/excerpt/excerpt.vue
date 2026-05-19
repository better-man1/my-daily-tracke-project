<template>
  <view class="page-container">
    <dt-navbar title="感悟摘录">
      <template #right>
        <view class="nav-add-btn" @tap="goAdd">
          <text class="nav-add-icon">+</text>
        </view>
      </template>
    </dt-navbar>

    <view class="page-content">
      <!-- 随机一句 -->
      <view v-if="randomItem" class="random-card fade-in" @tap="loadRandom">
        <view class="random-header">
          <text class="random-label">✨ 随机回顾</text>
          <text class="random-refresh">换一条 ↻</text>
        </view>
        <text class="random-content">{{ randomItem.content }}</text>
        <text v-if="randomItem.sourceTitle" class="random-source">—— {{ randomItem.sourceTitle }}</text>
        <view class="random-footer">
          <text class="random-date">{{ randomItem.excerptDate }}</text>
          <view v-if="randomItem.thought" class="random-thought">
            <text class="random-thought-text">💭 {{ randomItem.thought }}</text>
          </view>
        </view>
      </view>

      <!-- 筛选行 -->
      <view class="filter-row">
        <scroll-view scroll-x class="source-scroll">
          <view class="source-tags">
            <view v-for="s in sourceTypes" :key="s.value"
              class="source-tag"
              :class="{ 'source-tag--active': activeSource === s.value }"
              @tap="activeSource = s.value; loadList()">
              <text class="source-tag__text">{{ s.label }}</text>
            </view>
          </view>
        </scroll-view>

        <view class="favorite-toggle"
          :class="{ 'favorite-toggle--active': onlyFavorite }"
          @tap="onlyFavorite = !onlyFavorite; loadList()">
          <text class="favorite-icon">{{ onlyFavorite ? '❤️' : '🤍' }}</text>
        </view>
      </view>

      <!-- 摘录列表 -->
      <view v-if="excerpts.length > 0" class="excerpt-list">
        <view v-for="(item, idx) in excerpts" :key="item.id"
          class="excerpt-card slide-up"
          :style="{ animationDelay: (idx * 0.05) + 's' }">

          <!-- 收藏按钮 -->
          <view class="excerpt-fav" @tap.stop="toggleFavorite(item)">
            <text class="fav-icon">{{ item.isFavorite ? '❤️' : '🤍' }}</text>
          </view>

          <!-- 内容 -->
          <view class="excerpt-body" @tap="showDetail(item)">
            <text class="excerpt-content text-ellipsis-2">{{ item.content }}</text>

            <view v-if="item.thought" class="excerpt-thought">
              <text class="thought-prefix">💭</text>
              <text class="thought-text">{{ item.thought }}</text>
            </view>

            <view class="excerpt-meta">
              <view class="excerpt-source">
                <text class="source-type-badge">{{ item.sourceType || '其他' }}</text>
                <text v-if="item.sourceTitle" class="source-title">{{ item.sourceTitle }}</text>
              </view>
              <text class="excerpt-date">{{ item.excerptDate }}</text>
            </view>

            <view v-if="item.tags && item.tags.length > 0" class="excerpt-tags">
              <view v-for="tag in item.tags" :key="tag.id"
                class="excerpt-tag"
                :style="{ color: tag.color, background: tag.color + '18' }">
                <text># {{ tag.name }}</text>
              </view>
            </view>
          </view>

          <!-- 操作 -->
          <view class="excerpt-actions">
            <view class="action-del" @tap.stop="deleteExcerpt(item)">
              <text class="del-icon">🗑</text>
            </view>
          </view>
        </view>

        <view v-if="hasMore" class="load-more" @tap="loadMore">
          <text class="load-more-text">加载更多</text>
        </view>
        <view v-else class="list-end">
          <text class="list-end-text">— 已全部加载 —</text>
        </view>
      </view>

      <dt-empty v-else icon="📝" text="还没有摘录，记录点什么吧" action-text="写摘录" @action="goAdd" />
    </view>

    <!-- 详情弹窗 -->
    <view v-if="detailItem" class="detail-modal" @tap="detailItem = null">
      <view class="detail-panel" @tap.stop>
        <view class="detail-close" @tap="detailItem = null">
          <text class="close-icon">×</text>
        </view>
        <text class="detail-content">{{ detailItem.content }}</text>
        <view v-if="detailItem.thought" class="detail-thought">
          <text class="detail-thought-label">我的感悟</text>
          <text class="detail-thought-text">{{ detailItem.thought }}</text>
        </view>
        <view class="detail-info">
          <text class="detail-date">{{ detailItem.excerptDate }}</text>
          <text v-if="detailItem.sourceTitle">—— {{ detailItem.sourceTitle }}</text>
        </view>
      </view>
    </view>

    <!-- FAB -->
    <view class="fab-button" @tap="goAdd">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { excerptApi, type ExcerptItem } from '@/api/excerpt'

const excerpts = ref<ExcerptItem[]>([])
const randomItem = ref<ExcerptItem | null>(null)
const detailItem = ref<ExcerptItem | null>(null)
const activeSource = ref('ALL')
const onlyFavorite = ref(false)
const pageNum = ref(1)
const hasMore = ref(false)
const pageSize = 15

const sourceTypes = [
  { label: '全部', value: 'ALL' },
  { label: '书籍', value: 'BOOK' },
  { label: '文章', value: 'ARTICLE' },
  { label: '电影', value: 'MOVIE' },
  { label: '语录', value: 'QUOTE' },
  { label: '随想', value: 'THOUGHT' }
]

async function loadRandom() {
  randomItem.value = await excerptApi.getRandom().catch(() => null)
}

async function loadList() {
  pageNum.value = 1
  const params: any = { pageNum: 1, pageSize }
  if (activeSource.value !== 'ALL') params.sourceType = activeSource.value
  if (onlyFavorite.value) params.isFavorite = 1

  const res = await excerptApi.page(params).catch(() => null)
  if (res) {
    excerpts.value = res.records || []
    hasMore.value = res.pageNum < res.pages
  }
}

async function loadMore() {
  pageNum.value++
  const params: any = { pageNum: pageNum.value, pageSize }
  if (activeSource.value !== 'ALL') params.sourceType = activeSource.value
  if (onlyFavorite.value) params.isFavorite = 1

  const res = await excerptApi.page(params).catch(() => null)
  if (res) {
    excerpts.value.push(...(res.records || []))
    hasMore.value = res.pageNum < res.pages
  }
}

async function toggleFavorite(item: ExcerptItem) {
  await excerptApi.toggleFavorite(item.id).catch(() => null)
  item.isFavorite = item.isFavorite ? 0 : 1
}

function showDetail(item: ExcerptItem) {
  detailItem.value = item
}

function deleteExcerpt(item: ExcerptItem) {
  uni.showModal({
    title: '删除摘录',
    content: '确定删除这条摘录吗？',
    success: async (res) => {
      if (res.confirm) {
        await excerptApi.delete(item.id).catch(() => null)
        excerpts.value = excerpts.value.filter(e => e.id !== item.id)
        uni.showToast({ title: '已删除', icon: 'success' })
      }
    }
  })
}

function goAdd() {
  uni.navigateTo({ url: '/sub-pages/excerpt-add/excerpt-add' })
}

onShow(() => {
  loadRandom()
  loadList()
})
</script>

<style lang="scss" scoped>
// 随机卡片
.random-card {
  background: linear-gradient(135deg, #1E293B 0%, #0F172A 100%);
  border-radius: $dt-radius-xl;
  padding: 32rpx;
  margin-bottom: $dt-space-lg;
  box-shadow: $dt-shadow-md;

  @media (prefers-color-scheme: dark) {
    background: linear-gradient(135deg, #334155 0%, #1E293B 100%);
  }
}

.random-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.random-label {
  font-size: $dt-font-sm;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
}

.random-refresh {
  font-size: $dt-font-sm;
  color: $dt-primary-light;
}

.random-content {
  font-size: $dt-font-lg;
  color: #F1F5F9;
  line-height: 1.8;
  margin-bottom: 16rpx;
}

.random-source {
  font-size: $dt-font-sm;
  color: rgba(255, 255, 255, 0.5);
  text-align: right;
  display: block;
  margin-bottom: 16rpx;
}

.random-footer {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.random-date {
  font-size: $dt-font-xs;
  color: rgba(255, 255, 255, 0.35);
}

.random-thought {
  background: rgba(255, 255, 255, 0.05);
  border-radius: $dt-radius-sm;
  padding: 12rpx 16rpx;

  &-text {
    font-size: $dt-font-sm;
    color: rgba(255, 255, 255, 0.6);
  }
}

// 筛选行
.filter-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: $dt-space-lg;
}

.source-scroll {
  flex: 1;
  white-space: nowrap;
}

.source-tags {
  display: inline-flex;
  gap: 12rpx;
}

.source-tag {
  display: inline-flex;
  padding: 12rpx 24rpx;
  border-radius: $dt-radius-full;
  background: $dt-bg-card;
  box-shadow: $dt-shadow-sm;
  transition: all 0.2s;
  white-space: nowrap;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &--active {
    background: $dt-primary;
    box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.3);
  }

  &__text {
    font-size: $dt-font-sm;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &--active &__text {
    color: #FFFFFF;
    font-weight: 500;
  }
}

.favorite-toggle {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: $dt-bg-card;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $dt-shadow-sm;
  flex-shrink: 0;
  transition: all 0.2s;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &--active {
    background: rgba(239, 68, 68, 0.1);
  }

  .favorite-icon {
    font-size: 36rpx;
  }
}

// 摘录列表
.excerpt-list {
  padding-bottom: 120rpx;
}

.excerpt-card {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 28rpx 28rpx 28rpx 24rpx;
  margin-bottom: 16rpx;
  box-shadow: $dt-shadow-sm;
  display: flex;
  gap: 16rpx;
  position: relative;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  // 左侧装饰线
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 16rpx;
    bottom: 16rpx;
    width: 4rpx;
    background: linear-gradient(180deg, $dt-primary, $dt-primary-light);
    border-radius: 2rpx;
  }
}

.excerpt-fav {
  flex-shrink: 0;
  padding-top: 4rpx;

  .fav-icon {
    font-size: 32rpx;
  }
}

.excerpt-body {
  flex: 1;
  min-width: 0;
}

.excerpt-content {
  font-size: $dt-font-md;
  color: $dt-text-primary;
  line-height: 1.7;
  margin-bottom: 12rpx;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.excerpt-thought {
  display: flex;
  gap: 8rpx;
  background: rgba(99, 102, 241, 0.06);
  border-radius: $dt-radius-sm;
  padding: 12rpx 16rpx;
  margin-bottom: 12rpx;

  .thought-prefix { font-size: 24rpx; flex-shrink: 0; }
  .thought-text {
    font-size: $dt-font-sm;
    color: $dt-text-secondary;
    line-height: 1.6;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }
}

.excerpt-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.excerpt-source {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.source-type-badge {
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
  background: $dt-primary-bg;
  color: $dt-primary;
  font-weight: 500;
}

.source-title {
  font-size: $dt-font-xs;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.excerpt-date {
  font-size: $dt-font-xs;
  color: $dt-text-placeholder;
}

.excerpt-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.excerpt-tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: $dt-radius-full;
}

.excerpt-actions {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  flex-shrink: 0;
}

.action-del {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  .del-icon { font-size: 32rpx; }
}

// 详情弹窗
.detail-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  animation: fadeIn 0.2s ease;
}

.detail-panel {
  background: $dt-bg-card;
  border-radius: $dt-radius-xl $dt-radius-xl 0 0;
  padding: 48rpx 40rpx calc(60rpx + env(safe-area-inset-bottom));
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  animation: slideUp 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.detail-close {
  position: absolute;
  right: 32rpx;
  top: 24rpx;

  .close-icon {
    font-size: 52rpx;
    color: $dt-text-secondary;
    font-weight: 300;
    line-height: 1;
  }
}

.detail-content {
  font-size: $dt-font-lg;
  color: $dt-text-primary;
  line-height: 1.8;
  display: block;
  margin-bottom: 32rpx;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.detail-thought {
  background: rgba(99, 102, 241, 0.06);
  border-radius: $dt-radius-md;
  padding: 24rpx;
  margin-bottom: 24rpx;

  &-label {
    font-size: $dt-font-sm;
    color: $dt-primary;
    font-weight: 600;
    display: block;
    margin-bottom: 12rpx;
  }

  &-text {
    font-size: $dt-font-md;
    color: $dt-text-secondary;
    line-height: 1.7;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }
}

.detail-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: $dt-font-sm;
  color: $dt-text-placeholder;
}

// 通用样式
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

.fab-button {
  position: fixed;
  right: 32rpx;
  bottom: calc(180rpx + env(safe-area-inset-bottom));
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #F59E0B, #FBBF24);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(245, 158, 11, 0.4);
  z-index: 100;

  &:active { transform: scale(0.9); }

  .fab-icon {
    color: #FFFFFF;
    font-size: 56rpx;
    font-weight: 300;
    line-height: 1;
  }
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
</style>
