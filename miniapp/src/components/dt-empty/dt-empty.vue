<template>
  <view class="dt-empty">
    <text class="dt-empty__icon">{{ icon }}</text>
    <text class="dt-empty__text">{{ text }}</text>
    <view v-if="actionText" class="dt-empty__action" @tap="$emit('action')">
      <text class="dt-empty__action-text">{{ actionText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  icon?: string
  text?: string
  actionText?: string
}>(), {
  icon: '📭',
  text: '暂无数据'
})

defineEmits<{
  (e: 'action'): void
}>()
</script>

<style lang="scss" scoped>
.dt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 32rpx;

  &__icon {
    font-size: 100rpx;
    margin-bottom: $dt-space-lg;
  }

  &__text {
    font-size: $dt-font-md;
    color: $dt-text-placeholder;
    margin-bottom: $dt-space-xl;
  }

  &__action {
    padding: 16rpx 48rpx;
    background: $dt-primary-bg;
    border-radius: $dt-radius-full;

    &-text {
      font-size: $dt-font-sm;
      color: $dt-primary;
      font-weight: 500;
    }
  }
}
</style>
