<template>
  <view class="page-container">
    <dt-navbar title="新增摘录" show-back />

    <view class="page-content">
      <!-- 内容输入 -->
      <view class="form-card fade-in">
        <text class="section-title">摘录内容 <text class="required">*</text></text>
        <textarea
          class="content-textarea"
          v-model="form.content"
          placeholder="在这里记录让你心动的文字..."
          placeholder-class="textarea-placeholder"
          maxlength="2000"
          :auto-height="true"
          :focus="true"
        />
        <text class="char-count">{{ form.content.length }} / 2000</text>
      </view>

      <!-- 感悟 -->
      <view class="form-card slide-up">
        <text class="section-title">💭 我的感悟</text>
        <textarea
          class="thought-textarea"
          v-model="form.thought"
          placeholder="读到这段话，你有什么感想？"
          placeholder-class="textarea-placeholder"
          maxlength="500"
          :auto-height="true"
        />
      </view>

      <!-- 来源信息 -->
      <view class="form-card slide-up">
        <text class="section-title">📚 来源信息</text>

        <!-- 来源类型 -->
        <view class="form-group">
          <text class="form-label">类型</text>
          <scroll-view scroll-x>
            <view class="source-types">
              <view v-for="s in sourceTypes" :key="s.value"
                class="source-chip"
                :class="{ 'source-chip--active': form.sourceType === s.value }"
                @tap="form.sourceType = s.value">
                <text class="source-chip-text">{{ s.icon }} {{ s.label }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- 来源标题 -->
        <view class="form-group">
          <text class="form-label">标题 / 作者</text>
          <input
            class="form-input"
            v-model="form.sourceTitle"
            placeholder="书名、文章名..."
            placeholder-class="input-placeholder"
            maxlength="100"
          />
        </view>
      </view>

      <!-- 标签 & 日期 -->
      <view class="form-card slide-up">
        <view class="date-row">
          <text class="section-title">📅 摘录日期</text>
          <picker mode="date" :value="form.excerptDate"
            @change="form.excerptDate = $event.detail.value">
            <view class="date-picker">
              <text class="date-value">{{ form.excerptDate }}</text>
              <text class="date-arrow">›</text>
            </view>
          </picker>
        </view>

        <!-- 收藏 -->
        <view class="fav-row" @tap="form.isFavorite = form.isFavorite ? 0 : 1">
          <text class="fav-label">标记为收藏</text>
          <text class="fav-icon">{{ form.isFavorite ? '❤️' : '🤍' }}</text>
        </view>
      </view>

      <!-- 提交 -->
      <button class="submit-btn" :loading="loading" @tap="handleSubmit">
        <text class="submit-text">保 存</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { excerptApi } from '@/api/excerpt'
import { getToday } from '@/utils/date'

const loading = ref(false)

const form = ref({
  content: '',
  thought: '',
  sourceType: 'BOOK',
  sourceTitle: '',
  excerptDate: getToday(),
  isFavorite: 0
})

const sourceTypes = [
  { icon: '📚', label: '书籍', value: 'BOOK' },
  { icon: '📰', label: '文章', value: 'ARTICLE' },
  { icon: '🎬', label: '电影', value: 'MOVIE' },
  { icon: '💬', label: '语录', value: 'QUOTE' },
  { icon: '💡', label: '随想', value: 'THOUGHT' },
  { icon: '📦', label: '其他', value: 'OTHER' }
]

async function handleSubmit() {
  if (!form.value.content.trim()) {
    uni.showToast({ title: '请输入摘录内容', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await excerptApi.create({
      content: form.value.content.trim(),
      thought: form.value.thought || undefined,
      sourceType: form.value.sourceType,
      sourceTitle: form.value.sourceTitle || undefined,
      excerptDate: form.value.excerptDate,
      isFavorite: form.value.isFavorite
    })
    uni.showToast({ title: '保存成功 ✨', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 800)
  } catch {
    uni.showToast({ title: '保存失败', icon: 'none' })
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

.section-title {
  font-size: $dt-font-md;
  font-weight: 600;
  color: $dt-text-primary;
  display: block;
  margin-bottom: 20rpx;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }

  .required { color: $dt-danger; }
}

.content-textarea {
  width: 100%;
  min-height: 200rpx;
  font-size: $dt-font-lg;
  color: $dt-text-primary;
  line-height: 1.8;
  background: transparent;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.thought-textarea {
  width: 100%;
  min-height: 120rpx;
  font-size: $dt-font-md;
  color: $dt-text-primary;
  line-height: 1.7;
  background: transparent;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.textarea-placeholder {
  color: $dt-text-placeholder;
  font-size: $dt-font-md;
}

.char-count {
  display: block;
  text-align: right;
  font-size: $dt-font-xs;
  color: $dt-text-placeholder;
  margin-top: 8rpx;
}

.form-group {
  margin-bottom: 24rpx;
  &:last-child { margin-bottom: 0; }
}

.form-label {
  font-size: $dt-font-sm;
  color: $dt-text-secondary;
  display: block;
  margin-bottom: 12rpx;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

// 来源类型
.source-types {
  display: inline-flex;
  gap: 12rpx;
  padding: 2rpx;
}

.source-chip {
  display: inline-flex;
  padding: 12rpx 24rpx;
  border-radius: $dt-radius-full;
  background: rgba(0, 0, 0, 0.04);
  white-space: nowrap;
  border: 2rpx solid transparent;
  transition: all 0.2s;

  @media (prefers-color-scheme: dark) { background: rgba(255, 255, 255, 0.06); }

  &--active {
    border-color: $dt-primary;
    background: $dt-primary-bg;
  }

  &-text {
    font-size: $dt-font-sm;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &--active &-text {
    color: $dt-primary;
    font-weight: 500;
  }
}

.form-input {
  width: 100%;
  height: 80rpx;
  background: rgba(0, 0, 0, 0.03);
  border-radius: $dt-radius-sm;
  padding: 0 20rpx;
  font-size: $dt-font-md;
  color: $dt-text-primary;
  box-sizing: border-box;

  @media (prefers-color-scheme: dark) {
    background: rgba(255, 255, 255, 0.05);
    color: $dt-dark-text-primary;
  }
}

.input-placeholder {
  color: $dt-text-placeholder;
  font-size: $dt-font-md;
}

// 日期 & 收藏
.date-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.date-picker {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.date-value {
  font-size: $dt-font-md;
  color: $dt-primary;
  font-weight: 500;
}

.date-arrow {
  font-size: 36rpx;
  color: $dt-primary;
  font-weight: 300;
}

.fav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
  border-top: 1rpx solid $dt-divider;

  @media (prefers-color-scheme: dark) { border-top-color: $dt-dark-border; }
}

.fav-label {
  font-size: $dt-font-md;
  color: $dt-text-primary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.fav-icon { font-size: 40rpx; }

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
    font-size: $dt-font-xl;
    font-weight: 700;
    letter-spacing: 8rpx;
  }
}
</style>
