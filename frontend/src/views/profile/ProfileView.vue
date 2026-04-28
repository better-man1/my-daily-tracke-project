<template>
  <div class="profile-view">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
    </div>
    <div class="card" style="max-width:480px">
      <div class="profile-avatar">
        <div class="avatar-circle">{{ (userStore.nickname || 'U')[0].toUpperCase() }}</div>
        <div>
          <div class="profile-name">{{ userStore.nickname }}</div>
          <div class="profile-username text-muted text-sm">@{{ userStore.username }}</div>
        </div>
      </div>

      <el-divider />

      <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()

async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
  userStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.profile-view {
  .profile-avatar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 8px;

    .avatar-circle {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: linear-gradient(135deg, $primary, #7c3aed);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 26px;
      font-weight: 700;
      color: white;
    }

    .profile-name { font-size: 18px; font-weight: 600; }
  }
}
</style>
