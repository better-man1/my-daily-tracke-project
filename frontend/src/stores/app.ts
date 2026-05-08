import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏状态
  const sidebarCollapsed = ref(false)

  // 全局加载状态
  const loading = ref(false)
  const loadingText = ref('加载中...')

  // 当前日期（用于数据筛选）
  const currentDate = ref(new Date().toISOString().split('T')[0])

  // 请求计数器（用于多个请求同时进行时的 loading 控制）
  const requestCount = ref(0)

  // 错误状态
  const globalError = ref<Error | null>(null)

  // 计算属性
  const isLoading = computed(() => requestCount.value > 0)

  // 操作方法
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setLoading(val: boolean, text?: string) {
    loading.value = val
    if (text) loadingText.value = text
  }

  function setCurrentDate(date: string) {
    currentDate.value = date
  }

  // 请求计数管理
  function incrementRequest() {
    requestCount.value++
  }

  function decrementRequest() {
    if (requestCount.value > 0) {
      requestCount.value--
    }
  }

  function resetRequestCount() {
    requestCount.value = 0
  }

  // 错误处理
  function setError(error: Error | null) {
    globalError.value = error
  }

  function clearError() {
    globalError.value = null
  }

  return {
    // 状态
    sidebarCollapsed,
    loading,
    loadingText,
    currentDate,
    requestCount,
    globalError,
    // 计算属性
    isLoading,
    // 方法
    toggleSidebar,
    setLoading,
    setCurrentDate,
    incrementRequest,
    decrementRequest,
    resetRequestCount,
    setError,
    clearError
  }
})
