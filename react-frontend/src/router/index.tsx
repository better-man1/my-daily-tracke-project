/**
 * ============================================================================
 * 【React Router 路由配置文件 (src/router/index.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 单页应用 (SPA) 与路由原理：
 *    - 传统多页应用每次跳转都会向服务器请求新的 HTML 文件，产生白屏和页面重载。
 *    - SPA 只有一个真正的 HTML 文件（`index.html`），页面的跳转完全由 JavaScript 在前端拦截 URL
 *      变化，并动态替换局部 DOM 结构来实现。React Router 就是为此而生的路由框架。
 *
 * 2. 【React 核心概念：代码分割与懒加载 (Code Splitting & Lazy Loading)】
 *    - 如果直接导入所有页面组件，打包后的 JS 文件会非常庞大，导致首屏加载缓慢。
 *    - `React.lazy()` 配合 `import()` 可以实现“路由懒加载”：只有在浏览器访问特定 URL 时，
 *      才会异步下载并加载对应的页面 JS 代码。
 *    - `<Suspense>` 是 React 的内置组件，当其子组件尚未加载完毕时（例如异步下载 `LoginPage.tsx` 的 JS 中），
 *      它会展示 `fallback` 属性里指定的 Loading 组件。
 */

import React, { Suspense, useEffect } from 'react'
import { createBrowserRouter, Navigate, Outlet, useLocation } from 'react-router-dom'
import { Spin } from 'antd'

// 导入状态管理库，用于判断用户是否已登录，从而进行路由拦截
import { useUserStore } from '@/stores/useUserStore'

/**
 * 懒加载页面组件
 * React.lazy 参数是一个返回 import() Promise 的函数。
 * 这样，每个页面最终会被单独打包成一个小的 JS 分片文件（Chunk）。
 */
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
 * 3. 【React 路由守卫：ProtectedRoute 受保护的路由】
 *    - 作用：只允许已登录的用户访问。若未登录，则自动重定向到登录页。
 *    - 为什么这里使用 Zustand 的 Hook：`useUserStore(state => state.isLoggedIn())` 会让该组件
 *      监听登录状态。当登录状态发生改变时，该保护路由组件会自动重新运行，保证安全性。
 *    - `useLocation()`：获取当前路由的完整信息（例如当前路径 `/dashboard`）。
 *    - `<Navigate to="/login" state={{ from: location }} replace />`：
 *      - `state={{ from: location }}`：将当前试图访问但被拦截的页面路径记录下来，并传递给登录页面，
 *        方便用户登录成功后自动跳回该路径。
 *      - `replace`：以“替换”模式进行重定向，不把当前被拦截的记录存入浏览器的历史栈中，防止用户按返回键时反复被拦截。
 *    - `<Outlet />`：React Router 的占位符组件，相当于 Vue 的 `<router-view>`。
 *      当父路由匹配成功时，子路由对应的具体页面会被渲染到 `<Outlet />` 所在的位置。
 */
function ProtectedRoute({ children }: { children?: React.ReactNode }) {
  const isLoggedIn = useUserStore(state => state.isLoggedIn())
  const location = useLocation()

  if (!isLoggedIn) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  // 如果传入了 children 则直接渲染，否则渲染嵌套路由的占位符 Outlet
  return children ? <>{children}</> : <Outlet />
}

/**
 * 4. 【React 路由守卫：PublicRoute 公开/访客路由】
 *    - 作用：只允许未登录的访客访问（例如登录页、注册页）。
 *    - 逻辑：如果用户已经登录，就不允许再次进入登录页面，直接强行跳转到控制台（`/dashboard`）。
 */
function PublicRoute({ children }: { children: React.ReactNode }) {
  const isLoggedIn = useUserStore(state => state.isLoggedIn())

  if (isLoggedIn) {
    return <Navigate to="/dashboard" replace />
  }

  return <>{children}</>
}

/**
 * 5. 【React 核心概念：useEffect 副作用钩子】
 *    - 作用：将 React 组件与外部系统同步（更新 DOM 属性、网络请求、定时器等）。
 *    - 在 React 中，修改浏览器标签标题 (`document.title`) 是一项不属于渲染本身的“外部副作用”。
 *    - `useEffect` 接收两个参数：
 *      - 参数 1：要执行的副作用回调函数。
 *      - 参数 2：依赖项数组 `[location.pathname]`。
 *        - 当 `location.pathname`（即 URL 路径）改变时，React 才会再次运行这个回调函数。
 *        - 如果数组为空 `[]`，则该副作用只会在组件挂载（Mount）时执行一次。
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
  }, [location.pathname]) // 只有当前路径改变时，才重新运行以更新标题

  return null // 该组件纯粹用来执行副作用，不渲染任何 HTML
}

/**
 * LoadingFallback — 加载中占位组件
 * 当路由的异步 JS 文件尚未下载完成时显示
 */
function LoadingFallback() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <Spin size="large" description="加载中..." />
    </div>
  )
}

/**
 * 6. 【React 核心概念：Suspense 包裹器】
 *    - 懒加载组件（React.lazy）必须放在 `<Suspense>` 的组件树下方运行，否则 React 在渲染空缺时会报错。
 *    - 这里封装了一个便捷组件，自动把子页面塞入 `<Suspense>` 并配好首屏加载骨架。
 */
function SuspensedPage({ children }: { children: React.ReactNode }) {
  return (
    <Suspense fallback={<LoadingFallback />}>
      {children}
    </Suspense>
  )
}

/**
 * 7. 【创建路由器配置 (createBrowserRouter)】
 *    - 使用 React Router 6 推荐的 DOM History 模式路由器。
 *    - 支持通过嵌套对象（nested routes）进行布局共用。
 */
const router = createBrowserRouter([
  // 登录页：必须包裹 PublicRoute，表示已登录用户无法访问
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
  // 注册页：必须包裹 PublicRoute
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
  // 主应用（需要登录）：最外层使用 ProtectedRoute 进行权限拦截
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <PageTitle />
        <SuspensedPage>
          {/* AppLayout 内部包含侧边栏，并且在内容区留有 <Outlet /> 用于渲染子页面 */}
          <AppLayout />
        </SuspensedPage>
      </ProtectedRoute>
    ),
    children: [
      // 默认的根路由重定向：当访问 '/' 时，自动重定向到 '/dashboard'
      {
        index: true,
        element: <Navigate to="/dashboard" replace />
      },
      // 以下所有子路由，均会被加载到 AppLayout 的 <Outlet /> 占位处
      {
        path: 'dashboard',
        element: <Suspense fallback={<LoadingFallback />}><DashboardPage /></Suspense>
      },
      {
        path: 'plan',
        element: <Suspense fallback={<LoadingFallback />}><PlanPage /></Suspense>
      },
      {
        path: 'accounting',
        element: <Suspense fallback={<LoadingFallback />}><AccountingPage /></Suspense>
      },
      {
        path: 'excerpt',
        element: <Suspense fallback={<LoadingFallback />}><ExcerptPage /></Suspense>
      },
      {
        path: 'summary',
        element: <Suspense fallback={<LoadingFallback />}><SummaryPage /></Suspense>
      },
      {
        path: 'goal',
        element: <Suspense fallback={<LoadingFallback />}><GoalPage /></Suspense>
      },
      {
        path: 'profile',
        element: <Suspense fallback={<LoadingFallback />}><ProfilePage /></Suspense>
      },
      {
        path: 'test',
        element: <Suspense fallback={<LoadingFallback />}><TestPage /></Suspense>
      }
    ]
  },
  // 兜底路由处理：如果用户访问了任何不存在的 URL，一律重定向至主看板
  {
    path: '*',
    element: <Navigate to="/dashboard" replace />
  }
])

export default router
