import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const loading = ref(false)
  const currentDate = ref(new Date().toISOString().split('T')[0])

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setLoading(val: boolean) {
    loading.value = val
  }

  function setCurrentDate(date: string) {
    currentDate.value = date
  }

  return {
    sidebarCollapsed, loading, currentDate,
    toggleSidebar, setLoading, setCurrentDate
  }
})
