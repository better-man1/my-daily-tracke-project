/**
 * ============================================================================
 * React Router 路由配置文件（修复版）
 * ============================================================================
 */

import React, { Suspense, useEffect } from 'react'
import { createBrowserRouter, Navigate, Outlet, useLocation } from 'react-router-dom'
import { Spin } from 'antd'

import { useUserStore } from '@/stores/useUserStore'

// 懒加载页面组件
const LoginPage = React.lazy(() => import('@/pages/login/LoginPage'))
const RegisterPage = React.lazy(() => import('@/pages/register/RegisterPage'))
const DashboardPage = React.lazy(() => import('@/pages/dashboard/DashboardPage'))
const PlanPage = React.lazy(() => import('@/pages/plan/PlanPage'))
const AccountingPage = React.lazy(() => import('@/pages/accounting/AccountingPage'))
const ExcerptPage = React.lazy(() => import('@/pages/excerpt/ExcerptPage'))
const SummaryPage = React.lazy(() => import('@/pages/summary/SummaryPage'))
const GoalPage = React.lazy(() => import('@/pages/goal/GoalPage'))
const ProfilePage = React.lazy(() => import('@/pages/profile/ProfilePage'))
const TestPage = React.lazy(() => import('@/pages/TestPage'))
const AppLayout = React.lazy(() => import('@/components/layout/AppLayout'))

/**
 * ProtectedRoute — 受保护的路由组件
 */
function ProtectedRoute({ children }: { children?: React.ReactNode }) {
  const isLoggedIn = useUserStore(state => state.isLoggedIn())
  const location = useLocation()

  if (!isLoggedIn) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return children ? <>{children}</> : <Outlet />
}

/**
 * PublicRoute — 公开路由组件
 */
function PublicRoute({ children }: { children: React.ReactNode }) {
  const isLoggedIn = useUserStore(state => state.isLoggedIn())

  if (isLoggedIn) {
    return <Navigate to="/dashboard" replace />
  }

  return <>{children}</>
}

/**
 * PageTitle — 页面标题更新组件
 */
function PageTitle() {
  const location = useLocation()

  useEffect(() => {
    const titles: Record<string, string> = {
      '/login': '登录 - DailyTracker',
      '/register': '注册 - DailyTracker',
      '/dashboard': '数据看板 - DailyTracker',
      '/plan': '每日计划 - DailyTracker',
      '/accounting': '每日记账 - DailyTracker',
      '/excerpt': '每日摘录 - DailyTracker',
      '/summary': '每日总结 - DailyTracker',
      '/goal': '目标管理 - DailyTracker',
      '/profile': '个人中心 - DailyTracker',
      '/test': '路由测试 - DailyTracker'
    }

    const title = titles[location.pathname] || 'DailyTracker'
    document.title = title
  }, [location.pathname])

  return null
}

/**
 * LoadingFallback — 加载中占位组件
 */
function LoadingFallback() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <Spin size="large" description="加载中..." />
    </div>
  )
}

/**
 * 带有Suspense的页面包装器
 */
function SuspensedPage({ children }: { children: React.ReactNode }) {
  return (
    <Suspense fallback={<LoadingFallback />}>
      {children}
    </Suspense>
  )
}

/**
 * 创建路由器实例
 */
const router = createBrowserRouter([
  // 登录页
  {
    path: '/login',
    element: (
      <PublicRoute>
        <PageTitle />
        <SuspensedPage>
          <LoginPage />
        </SuspensedPage>
      </PublicRoute>
    )
  },
  // 注册页
  {
    path: '/register',
    element: (
      <PublicRoute>
        <PageTitle />
        <SuspensedPage>
          <RegisterPage />
        </SuspensedPage>
      </PublicRoute>
    )
  },
  // 主应用（需要登录）
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <PageTitle />
        <SuspensedPage>
          <AppLayout />
        </SuspensedPage>
      </ProtectedRoute>
    ),
    children: [
      // 默认重定向到dashboard
      {
        index: true,
        element: <Navigate to="/dashboard" replace />
      },
      // 数据看板
      {
        path: 'dashboard',
        element: <Suspense fallback={<LoadingFallback />}><DashboardPage /></Suspense>
      },
      // 每日计划
      {
        path: 'plan',
        element: <Suspense fallback={<LoadingFallback />}><PlanPage /></Suspense>
      },
      // 每日记账
      {
        path: 'accounting',
        element: <Suspense fallback={<LoadingFallback />}><AccountingPage /></Suspense>
      },
      // 每日摘录
      {
        path: 'excerpt',
        element: <Suspense fallback={<LoadingFallback />}><ExcerptPage /></Suspense>
      },
      // 每日总结
      {
        path: 'summary',
        element: <Suspense fallback={<LoadingFallback />}><SummaryPage /></Suspense>
      },
      // 目标管理
      {
        path: 'goal',
        element: <Suspense fallback={<LoadingFallback />}><GoalPage /></Suspense>
      },
      // 个人中心
      {
        path: 'profile',
        element: <Suspense fallback={<LoadingFallback />}><ProfilePage /></Suspense>
      },
      // 测试页面
      {
        path: 'test',
        element: <Suspense fallback={<LoadingFallback />}><TestPage /></Suspense>
      }
    ]
  },
  // 404 处理
  {
    path: '*',
    element: <Navigate to="/dashboard" replace />
  }
])

export default router
