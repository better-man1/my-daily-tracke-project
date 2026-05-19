<template>
  <view class="dt-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
    <view class="dt-navbar__content" :style="{ height: navBarHeight + 'px' }">
      <!-- 左侧返回按钮 -->
      <view v-if="showBack" class="dt-navbar__left" @tap="handleBack">
        <text class="dt-navbar__back-icon">‹</text>
      </view>
      <view v-else class="dt-navbar__left">
        <slot name="left" />
      </view>

      <!-- 标题 -->
      <view class="dt-navbar__title">
        <text v-if="title" class="dt-navbar__title-text">{{ title }}</text>
        <slot v-else name="center" />
      </view>

      <!-- 右侧操作区 -->
      <view class="dt-navbar__right">
        <slot name="right" />
      </view>
    </view>
  </view>
  <!-- 占位 -->
  <view :style="{ height: (statusBarHeight + navBarHeight) + 'px' }" />
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  title?: string
  showBack?: boolean
}>()

const emit = defineEmits<{
  (e: 'back'): void
}>()

const statusBarHeight = ref(0)
const navBarHeight = ref(44)

// 获取状态栏高度
const systemInfo = uni.getSystemInfoSync()
statusBarHeight.value = systemInfo.statusBarHeight || 0

function handleBack() {
  emit('back')
  uni.navigateBack({ delta: 1 })
}
</script>

<style lang="scss" scoped>
.dt-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);

  @media (prefers-color-scheme: dark) {
    background: rgba(15, 23, 42, 0.95);
  }

  &__content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24rpx;
  }

  &__left {
    width: 100rpx;
    display: flex;
    align-items: center;
  }

  &__back-icon {
    font-size: 56rpx;
    font-weight: 300;
    color: $dt-text-primary;
    line-height: 1;
    padding: 0 16rpx;

    @media (prefers-color-scheme: dark) {
      color: $dt-dark-text-primary;
    }
  }

  &__title {
    flex: 1;
    text-align: center;

    &-text {
      font-size: 34rpx;
      font-weight: 600;
      color: $dt-text-primary;

      @media (prefers-color-scheme: dark) {
        color: $dt-dark-text-primary;
      }
    }
  }

  &__right {
    width: 100rpx;
    display: flex;
    align-items: center;
    justify-content: flex-end;
  }
}
</style>
