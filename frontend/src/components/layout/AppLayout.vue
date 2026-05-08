<template>
  <div class="app-layout" :class="{ 'sidebar-mini': appStore.sidebarCollapsed }">
    <!-- 全局 Loading -->
    <Loading v-if="appStore.loading" :text="appStore.loadingText" />
    <!-- 侧边栏 -->
    <aside class="sidebar glass">
      <!-- Logo -->
      <div class="sidebar-logo">
        <div class="logo-icon">📅</div>
        <span class="logo-text" v-show="!appStore.sidebarCollapsed">DailyTracker</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="sidebar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          :title="appStore.sidebarCollapsed ? item.name : ''"
        >
          <el-icon class="nav-icon"><component :is="iconMap[item.icon]" /></el-icon>
          <span class="nav-label" v-show="!appStore.sidebarCollapsed">{{ item.name }}</span>
        </router-link>
      </nav>

    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部栏 -->
      <header class="top-bar glass">
        <div class="top-bar-left">
          <el-button link class="top-action-btn" @click="appStore.toggleSidebar" title="收缩/展开侧边栏">
            <el-icon size="20"><component :is="appStore.sidebarCollapsed ? iconMap.Expand : iconMap.Fold" /></el-icon>
          </el-button>
          <h1 class="current-page-title">{{ currentPageTitle }}</h1>
          <span class="current-date">{{ today }}</span>
        </div>
        <div class="top-bar-right">
          <el-switch
            v-model="isDark"
            inline-prompt
            :active-icon="iconMap.Moon"
            :inactive-icon="iconMap.Sunny"
            style="--el-switch-on-color: #2c2c2c; margin-right: 16px"
          />
          
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-profile-link">
              <div class="user-avatar">
                <img v-if="userStore.avatar" :src="userStore.avatar" alt="avatar" />
                <span v-else>{{ (userStore.nickname || 'U')[0].toUpperCase() }}</span>
              </div>
              <span class="nickname">{{ userStore.nickname }}</span>
              <el-icon class="el-icon--right"><component :is="iconMap.ArrowDown" /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><component :is="iconMap.User" /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided class="danger">
                  <el-icon><component :is="iconMap.SwitchButton" /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <div class="page-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" :key="$route.path" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useDark } from '@vueuse/core'
import Loading from '@/components/Loading.vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

import {
  DataLine,
  Calendar,
  CreditCard,
  Notebook,
  EditPen,
  TrophyBase,
  Expand,
  Fold,
  SwitchButton,
  Moon,
  Sunny,
  ArrowDown,
  User
} from '@element-plus/icons-vue'

const iconMap: Record<string, any> = {
  DataLine,
  Calendar,
  CreditCard,
  Notebook,
  EditPen,
  TrophyBase,
  Expand,
  Fold,
  SwitchButton,
  Moon,
  Sunny,
  ArrowDown,
  User
}

dayjs.locale('zh-cn')

const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const isDark = useDark()

const today = computed(() => dayjs().format('YYYY年MM月DD日 dddd'))

const navItems = [
  { path: '/dashboard', name: '数据看板', icon: 'DataLine' },
  { path: '/plan', name: '每日计划', icon: 'Calendar' },
  { path: '/accounting', name: '每日记账', icon: 'CreditCard' },
  { path: '/excerpt', name: '每日摘录', icon: 'Notebook' },
  { path: '/summary', name: '每日总结', icon: 'EditPen' },
  { path: '/goal', name: '目标管理', icon: 'TrophyBase' }
]

const currentPageTitle = computed(
  () => (route.meta.title as string)?.split(' - ')[0] || 'DailyTracker'
)

function isActive(path: string) {
  return route.path.startsWith(path)
}

function handleUserCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    handleLogout()
  }
}

async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'logout-confirm-box'
  })
  userStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;

  .sidebar {
    width: 220px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    padding: 16px 12px;
    border-right: 1px solid $border;
    border-radius: 0;
    transition: width 0.3s ease;
    overflow: hidden;
  }

  &.sidebar-mini .sidebar {
    width: 64px;
  }

  .sidebar-logo {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 8px 20px;
    border-bottom: 1px solid $border;
    margin-bottom: 12px;

    .logo-icon {
      font-size: 24px;
      flex-shrink: 0;
    }

    .logo-text {
      font-size: 16px;
      font-weight: 700;
      background: linear-gradient(135deg, $primary-light, #a78bfa);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      white-space: nowrap;
    }
  }

  .sidebar-nav {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
    overflow-y: auto;
  }

  .nav-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 10px;
    border-radius: $radius-md;
    color: $text-secondary;
    cursor: pointer;
    transition: $transition-fast;
    white-space: nowrap;
    background: none;
    border: none;
    width: 100%;
    text-align: left;
    font-size: 14px;
    text-decoration: none;

    &:hover {
      background: var(--nav-hover-bg);
      color: $text-primary;
    }

    &.active {
      background: rgba($primary, 0.15);
      color: $primary-light;

      .nav-icon {
        color: $primary-light;
      }
    }

    .nav-icon {
      font-size: 18px;
      flex-shrink: 0;
      width: 20px;
    }

    .nav-label {
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    min-width: 0;
  }

  .top-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 24px;
    border-bottom: 1px solid $border;
    border-radius: 0;
    flex-shrink: 0;
    backdrop-filter: blur(8px);

    .top-bar-left {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .top-bar-right {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .current-page-title {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
    }

    .current-date {
      font-size: 13px;
      color: $text-muted;
    }

    .top-action-btn {
      color: $text-secondary;
      margin-right: 12px;
      &:hover {
        color: $text-primary;
      }
    }

    .user-profile-link {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 12px;
      border-radius: $radius-full;
      cursor: pointer;
      transition: $transition-fast;
      outline: none;

      &:hover {
        background: var(--nav-hover-bg);
      }

      .user-avatar {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: linear-gradient(135deg, $primary, #a78bfa);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 700;
        color: white;
        flex-shrink: 0;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .nickname {
        font-size: 14px;
        font-weight: 500;
        color: $text-primary;
      }
    }
  }

  .page-wrapper {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
  }
}

// 路由切换动画
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
