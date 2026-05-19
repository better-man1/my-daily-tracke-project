/**
 * 用户状态管理 — Pinia Store
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  userId: number
  username: string
  nickname: string
  avatar: string | null
  accessToken: string
  refreshToken: string
}

export const useUserStore = defineStore('user', () => {
  // 状态 — 从本地存储初始化
  const userInfo = ref<Partial<UserInfo>>(
    JSON.parse(uni.getStorageSync('user_info') || '{}')
  )
  const accessToken = ref<string>(uni.getStorageSync('access_token') || '')
  const refreshToken = ref<string>(uni.getStorageSync('refresh_token') || '')

  // 计算属性
  const isLoggedIn = computed(() => !!accessToken.value)
  const userId = computed(() => userInfo.value.userId)
  const nickname = computed(() => userInfo.value.nickname || userInfo.value.username || '用户')
  const avatar = computed(() => userInfo.value.avatar)

  /** 登录成功后保存数据 */
  function setLoginData(data: UserInfo) {
    userInfo.value = data
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    uni.setStorageSync('user_info', JSON.stringify(data))
    uni.setStorageSync('access_token', data.accessToken)
    uni.setStorageSync('refresh_token', data.refreshToken)
  }

  /** 退出登录 */
  function logout() {
    userInfo.value = {}
    accessToken.value = ''
    refreshToken.value = ''
    uni.removeStorageSync('user_info')
    uni.removeStorageSync('access_token')
    uni.removeStorageSync('refresh_token')
  }

  /** 更新用户信息 */
  function updateUserInfo(data: Partial<UserInfo>) {
    userInfo.value = { ...userInfo.value, ...data }
    uni.setStorageSync('user_info', JSON.stringify(userInfo.value))
  }

  return {
    userInfo,
    accessToken,
    refreshToken,
    isLoggedIn,
    userId,
    nickname,
    avatar,
    setLoginData,
    logout,
    updateUserInfo
  }
})
