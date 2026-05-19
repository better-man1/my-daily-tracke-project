<template>
  <view class="dt-card" :class="{ 'dt-card--no-padding': noPadding }" @tap="$emit('tap')">
    <!-- 卡片头部 -->
    <view v-if="title || $slots.header" class="dt-card__header">
      <view class="dt-card__header-left">
        <text v-if="icon" class="dt-card__icon">{{ icon }}</text>
        <text v-if="title" class="dt-card__title">{{ title }}</text>
      </view>
      <view class="dt-card__header-right">
        <slot name="header-right" />
      </view>
    </view>
    <!-- 卡片内容 -->
    <view class="dt-card__body">
      <slot />
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  icon?: string
  noPadding?: boolean
}>()

defineEmits<{
  (e: 'tap'): void
}>()
</script>

<style lang="scss" scoped>
.dt-card {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: $dt-space-xl;
  margin-bottom: $dt-space-lg;
  box-shadow: $dt-shadow-sm;
  transition: all 0.2s ease;

  @media (prefers-color-scheme: dark) {
    background: $dt-dark-bg-card;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
  }

  &--no-padding {
    padding: 0;

    .dt-card__header {
      padding: $dt-space-xl $dt-space-xl 0;
    }

    .dt-card__body {
      padding: 0;
    }
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $dt-space-lg;

    &-left {
      display: flex;
      align-items: center;
      gap: $dt-space-sm;
    }
  }

  &__icon {
    font-size: 36rpx;
  }

  &__title {
    font-size: $dt-font-lg;
    font-weight: 600;
    color: $dt-text-primary;

    @media (prefers-color-scheme: dark) {
      color: $dt-dark-text-primary;
    }
  }
}
</style>
