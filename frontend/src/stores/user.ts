import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginResponse } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<Partial<LoginResponse>>(
    JSON.parse(localStorage.getItem('user_info') || '{}')
  )
  const accessToken = ref<string>(localStorage.getItem('access_token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refresh_token') || '')

  const isLoggedIn = computed(() => !!accessToken.value)
  const userId = computed(() => userInfo.value.userId)
  const username = computed(() => userInfo.value.username)
  const nickname = computed(() => userInfo.value.nickname || userInfo.value.username)
  const avatar = computed(() => userInfo.value.avatar)

  function setLoginData(data: LoginResponse) {
    userInfo.value = data
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    localStorage.setItem('user_info', JSON.stringify(data))
    localStorage.setItem('access_token', data.accessToken)
    localStorage.setItem('refresh_token', data.refreshToken)
  }

  function logout() {
    userInfo.value = {}
    accessToken.value = ''
    refreshToken.value = ''
    localStorage.removeItem('user_info')
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
  }

  function updateUserInfo(data: Partial<LoginResponse>) {
    userInfo.value = { ...userInfo.value, ...data }
    localStorage.setItem('user_info', JSON.stringify(userInfo.value))
  }

  return {
    userInfo, accessToken, refreshToken,
    isLoggedIn, userId, username, nickname, avatar,
    setLoginData, logout, updateUserInfo
  }
})
