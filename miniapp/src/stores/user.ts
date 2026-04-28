import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface UserInfo {
  userId?: number
  username?: string
  nickname?: string
  avatar?: string | null
  accessToken?: string
  refreshToken?: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo>(
    (() => {
      try {
        return JSON.parse(uni.getStorageSync('user_info') || '{}')
      } catch {
        return {}
      }
    })()
  )

  const accessToken = ref<string>(uni.getStorageSync('access_token') || '')

  const isLoggedIn = computed(() => !!accessToken.value)
  const nickname = computed(() => userInfo.value.nickname || userInfo.value.username || '用户')
  const avatar = computed(() => userInfo.value.avatar)
  const userId = computed(() => userInfo.value.userId)

  function setLoginData(data: any) {
    userInfo.value = data
    accessToken.value = data.accessToken || ''
    uni.setStorageSync('user_info', JSON.stringify(data))
    uni.setStorageSync('access_token', data.accessToken || '')
    uni.setStorageSync('refresh_token', data.refreshToken || '')
  }

  function logout() {
    userInfo.value = {}
    accessToken.value = ''
    uni.removeStorageSync('user_info')
    uni.removeStorageSync('access_token')
    uni.removeStorageSync('refresh_token')
  }

  return {
    userInfo, accessToken,
    isLoggedIn, nickname, avatar, userId,
    setLoginData, logout
  }
})
