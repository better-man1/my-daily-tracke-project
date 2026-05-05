<template>
  <div class="app-layout" :class="{ 'sidebar-mini': appStore.sidebarCollapsed }">
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
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span class="nav-label" v-show="!appStore.sidebarCollapsed">{{ item.name }}</span>
        </router-link>
      </nav>


    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部栏 -->
      <header class="top-bar glass">
        <div class="top-bar-left">
          <!-- 收缩按钮 -->
          <button class="collapse-top-btn" @click="appStore.toggleSidebar" :title="appStore.sidebarCollapsed ? '展开侧栏' : '收缩侧栏'">
            <el-icon><component :is="appStore.sidebarCollapsed ? 'Expand' : 'Fold'" /></el-icon>
          </button>
          <div class="top-divider-v" />
          <h1 class="current-page-title">{{ currentPageTitle }}</h1>
          <span class="current-date">{{ today }}</span>
        </div>
        <div class="top-bar-right">
          <!-- 用户菜单（头像点击展开） -->
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-nav-btn">
              <div class="user-avatar-sm">
                <img v-if="userStore.avatar" :src="userStore.avatar" alt="avatar" />
                <span v-else>{{ (userStore.nickname || 'U')[0].toUpperCase() }}</span>
              </div>
              <span class="user-nickname">{{ userStore.nickname }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided class="logout-item">
                  <el-icon><SwitchButton /></el-icon>退出登录
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
import { ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
dayjs.locale('zh-cn')

const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const today = computed(() =>
  dayjs().format('YYYY年MM月DD日 dddd')
)

const navItems = [
  { path: '/dashboard',  name: '数据看板', icon: 'DataLine' },
  { path: '/plan',       name: '每日计划', icon: 'Calendar' },
  { path: '/accounting', name: '每日记账', icon: 'CreditCard' },
  { path: '/excerpt',    name: '每日摘录', icon: 'Notebook' },
  { path: '/summary',    name: '每日总结', icon: 'EditPen' },
  { path: '/goal',       name: '目标管理', icon: 'TrophyBase' },
]

const currentPageTitle = computed(
  () => (route.meta.title as string)?.split(' - ')[0] || 'DailyTracker'
)

function isActive(path: string) {
  return route.path.startsWith(path)
}

async function handleUserCommand(cmd: string) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
  }
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
      background: rgba(255, 255, 255, 0.06);
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

  // 顶部栏收缩按钮
  .collapse-top-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: $radius-sm;
    border: none;
    background: transparent;
    color: $text-muted;
    cursor: pointer;
    transition: $transition-fast;
    flex-shrink: 0;

    .el-icon { font-size: 18px; }

    &:hover {
      background: rgba(255, 255, 255, 0.07);
      color: $text-primary;
    }
  }

  .top-divider-v {
    width: 1px;
    height: 18px;
    background: $border;
    flex-shrink: 0;
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
      align-items: baseline;
      gap: 12px;
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

    .top-bar-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    // 用户导航按鈕
    .user-nav-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 10px;
      border-radius: $radius-md;
      text-decoration: none;
      color: $text-secondary;
      transition: $transition-fast;

      &:hover {
        background: rgba(255, 255, 255, 0.06);
        color: $text-primary;
      }

      .user-avatar-sm {
        width: 30px;
        height: 30px;
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
        border: 2px solid rgba($primary, 0.4);

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .user-nickname {
        font-size: 13px;
        font-weight: 500;
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .dropdown-arrow {
        font-size: 12px;
        color: $text-muted;
        transition: transform 0.2s ease;
      }

      &:hover .dropdown-arrow {
        color: $text-primary;
      }
    }

    .top-divider {
      width: 1px;
      height: 18px;
      background: $border;
    }

    .logout-btn {
      color: $text-muted;
      font-size: 13px;

      &:hover {
        color: $danger;
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
