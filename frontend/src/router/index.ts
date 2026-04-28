import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // 登录/注册
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { public: true, title: '登录 - DailyTracker' }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/register/RegisterView.vue'),
      meta: { public: true, title: '注册 - DailyTracker' }
    },
    // 主应用（需要登录）
    {
      path: '/',
      component: () => import('@/components/layout/AppLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: { title: '数据看板 - DailyTracker', icon: 'DataLine' }
        },
        {
          path: 'plan',
          name: 'Plan',
          component: () => import('@/views/plan/PlanView.vue'),
          meta: { title: '每日计划 - DailyTracker', icon: 'Calendar' }
        },
        {
          path: 'accounting',
          name: 'Accounting',
          component: () => import('@/views/accounting/AccountingView.vue'),
          meta: { title: '每日记账 - DailyTracker', icon: 'CreditCard' }
        },
        {
          path: 'excerpt',
          name: 'Excerpt',
          component: () => import('@/views/excerpt/ExcerptView.vue'),
          meta: { title: '每日摘录 - DailyTracker', icon: 'Notebook' }
        },
        {
          path: 'summary',
          name: 'Summary',
          component: () => import('@/views/summary/SummaryView.vue'),
          meta: { title: '每日总结 - DailyTracker', icon: 'EditPen' }
        },
        {
          path: 'goal',
          name: 'Goal',
          component: () => import('@/views/goal/GoalView.vue'),
          meta: { title: '目标管理 - DailyTracker', icon: 'TrophyBase' }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/profile/ProfileView.vue'),
          meta: { title: '个人中心 - DailyTracker', icon: 'User' }
        }
      ]
    },
    // 404
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
})

// 全局路由守卫
router.beforeEach((to, _from, next) => {
  // 更新页面标题
  document.title = (to.meta.title as string) || 'DailyTracker'

  const userStore = useUserStore()
  if (to.meta.public) {
    // 已登录则重定向到首页
    if (userStore.isLoggedIn) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    // 未登录重定向到登录页
    if (!userStore.isLoggedIn) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
