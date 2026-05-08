<template>
  <div v-if="error" class="error-boundary">
    <div class="error-card card">
      <div class="error-icon">⚠️</div>
      <h3 class="error-title">{{ title }}</h3>
      <p class="error-message">{{ message }}</p>
      <div class="error-actions">
        <el-button type="primary" @click="retry">重试</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
      <details v-if="showDetails" class="error-details">
        <summary>错误详情</summary>
        <pre>{{ errorDetails }}</pre>
      </details>
    </div>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

interface Props {
  error?: Error | null
  title?: string
  message?: string
  showDetails?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '出错了',
  message: '页面加载失败，请重试',
  showDetails: false
})

const emit = defineEmits<{
  retry: []
}>()

const router = useRouter()

const errorDetails = computed(() => {
  if (!props.error) return ''
  return props.error.stack || props.error.message
})

function retry() {
  emit('retry')
}

function goHome() {
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 20px;
}

.error-card {
  max-width: 480px;
  padding: 40px;
  text-align: center;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.error-title {
  font-size: 20px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 12px;
}

.error-message {
  font-size: 14px;
  color: $text-secondary;
  margin-bottom: 24px;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 16px;
}

.error-details {
  text-align: left;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid $border;

  summary {
    cursor: pointer;
    color: $text-muted;
    font-size: 13px;
    padding: 8px 0;

    &:hover {
      color: $text-secondary;
    }
  }

  pre {
    background: $bg-input;
    padding: 12px;
    border-radius: $radius-sm;
    overflow-x: auto;
    font-size: 12px;
    color: $text-muted;
    margin-top: 8px;
    max-height: 200px;
    overflow-y: auto;
  }
}
</style>
