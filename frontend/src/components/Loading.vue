<template>
  <div class="loading-container" :class="{ 'loading-fullscreen': fullscreen }">
    <div class="loading-spinner">
      <div class="spinner-ring" v-for="i in 3" :key="i" :style="{ animationDelay: `${i * 0.15}s` }"></div>
    </div>
    <p v-if="text" class="loading-text">{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
interface Props {
  text?: string
  fullscreen?: boolean
}
withDefaults(defineProps<Props>(), {
  text: '加载中...',
  fullscreen: true
})
</script>

<style lang="scss" scoped>
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;

  &.loading-fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba($bg-page, 0.8);
    backdrop-filter: blur(4px);
    z-index: 9999;
  }
}

.loading-spinner {
  display: flex;
  gap: 8px;
  align-items: center;
}

.spinner-ring {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: $primary;
  animation: pulse 1.4s ease-in-out infinite;
}

@keyframes pulse {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

.loading-text {
  margin-top: 16px;
  font-size: 14px;
  color: $text-secondary;
  font-weight: 500;
}
</style>
