<template>
  <div class="skeleton-wrapper">
    <!-- 文本骨架 -->
    <template v-if="type === 'text'">
      <div
        v-for="i in rows"
        :key="i"
        class="skeleton skeleton-text"
        :style="{ width: i === rows ? '60%' : '100%', height: height }"
      />
    </template>

    <!-- 头像骨架 -->
    <template v-else-if="type === 'avatar'">
      <div class="skeleton skeleton-avatar" :style="{ width: size, height: size }" />
    </template>

    <!-- 卡片骨架 -->
    <template v-else-if="type === 'card'">
      <div class="skeleton-card">
        <div class="skeleton skeleton-card-header" :style="{ height: headerHeight }" />
        <div class="skeleton skeleton-card-body">
          <div class="skeleton skeleton-text" style="width: 100%; height: 16px" />
          <div class="skeleton skeleton-text" style="width: 80%; height: 16px" />
          <div class="skeleton skeleton-text" style="width: 60%; height: 16px" />
        </div>
      </div>
    </template>

    <!-- 列表骨架 -->
    <template v-else-if="type === 'list'">
      <div v-for="i in count" :key="i" class="skeleton-list-item">
        <div class="skeleton skeleton-avatar" style="width: 40px; height: 40px; flex-shrink: 0" />
        <div class="skeleton-list-content">
          <div class="skeleton skeleton-text" style="width: 100%; height: 16px; margin-bottom: 8px" />
          <div class="skeleton skeleton-text" style="width: 70%; height: 14px" />
        </div>
      </div>
    </template>

    <!-- 仪表盘统计卡片骨架 -->
    <template v-else-if="type === 'stat-card'">
      <div class="skeleton-stat-card">
        <div class="skeleton skeleton-stat-icon" />
        <div class="skeleton skeleton-text" style="width: 60%; height: 24px; margin-bottom: 8px" />
        <div class="skeleton skeleton-text" style="width: 40%; height: 14px" />
      </div>
    </template>

    <!-- 图表骨架 -->
    <template v-else-if="type === 'chart'">
      <div class="skeleton-chart">
        <div class="skeleton skeleton-text" style="width: 30%; height: 16px; margin-bottom: 16px" />
        <div class="skeleton skeleton-chart-body" />
      </div>
    </template>

    <!-- 自定义骨架 -->
    <div v-else class="skeleton" :style="{ width, height }" />
  </div>
</template>

<script setup lang="ts">
interface Props {
  type?: 'text' | 'avatar' | 'card' | 'list' | 'stat-card' | 'chart' | 'custom'
  rows?: number
  height?: string
  width?: string
  size?: string
  count?: number
  headerHeight?: string
}

withDefaults(defineProps<Props>(), {
  type: 'text',
  rows: 3,
  height: '16px',
  width: '100%',
  size: '40px',
  count: 3,
  headerHeight: '120px'
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.skeleton-wrapper {
  width: 100%;
}

.skeleton {
  background: linear-gradient(90deg, var(--bg-card) 25%, var(--bg-card-hover) 50%, var(--bg-card) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: $radius-sm;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

// 头像骨架
.skeleton-avatar {
  border-radius: 50%;
  flex-shrink: 0;
}

// 卡片骨架
.skeleton-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: $radius-lg;
  padding: $space-lg;
  overflow: hidden;

  .skeleton-card-header {
    width: 100%;
    margin-bottom: $space-md;
    border-radius: $radius-sm;
  }

  .skeleton-card-body {
    display: flex;
    flex-direction: column;
    gap: $space-sm;
  }
}

// 列表骨架
.skeleton-list-item {
  display: flex;
  align-items: center;
  gap: $space-md;
  padding: $space-md 0;
  border-bottom: 1px solid var(--border);

  &:last-child {
    border-bottom: none;
  }

  .skeleton-list-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
}

// 统计卡片骨架
.skeleton-stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: $radius-lg;
  padding: $space-lg;
  height: 140px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  .skeleton-stat-icon {
    width: 48px;
    height: 48px;
    border-radius: $radius-md;
    margin-bottom: $space-md;
  }
}

// 图表骨架
.skeleton-chart {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: $radius-lg;
  padding: $space-lg;
  height: 280px;

  .skeleton-chart-body {
    flex: 1;
    border-radius: $radius-sm;
  }
}
</style>
