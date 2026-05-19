<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'

onLaunch(() => {
  console.log('[DailyTracker] App launched')
  const userStore = useUserStore()
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/sub-pages/login/login' })
  }
})

onShow(() => {
  console.log('[DailyTracker] App shown')
})
</script>

<style lang="scss">
@import '@/styles/common.scss';
</style>
