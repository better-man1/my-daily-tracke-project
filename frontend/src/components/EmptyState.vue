<template>
  <div class="empty-state" :class="{ 'empty-state-compact': compact }">
    <div v-if="icon" class="empty-icon">{{ icon }}</div>
    <div v-else class="empty-icon default-icon">
      <el-icon :size="48" color="var(--text-muted)"><component :is="iconComponent" /></el-icon>
    </div>
    <h3 v-if="title" class="empty-title">{{ title }}</h3>
    <p v-if="description" class="empty-description">{{ description }}</p>
    <div v-if="$slots.action" class="empty-action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Document, Box, Calendar, Money, NoteBook, Trophy, Edit, FolderOpened } from '@element-plus/icons-vue'

interface Props {
  type?: 'data' | 'plan' | 'accounting' | 'excerpt' | 'summary' | 'goal' | 'default'
  icon?: string
  title?: string
  description?: string
  compact?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  title: '暂无数据',
  description: '当前没有相关内容',
  compact: false
})

const iconMap = {
  data: Document,
  plan: Calendar,
  accounting: Money,
  excerpt: NoteBook,
  summary: Edit,
  goal: Trophy,
  default: FolderOpened
}

const iconComponent = computed(() => iconMap[props.type])
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
  min-height: 200px;

  &.empty-state-compact {
    padding: 32px 16px;
    min-height: 120px;
  }

  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
    opacity: 0.8;

    &.default-icon {
      color: var(--text-muted);
    }
  }

  .empty-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 8px;
  }

  .empty-description {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 20px;
    max-width: 300px;
    line-height: 1.5;
  }

  .empty-action {
    display: flex;
    gap: 12px;
  }
}
</style>
