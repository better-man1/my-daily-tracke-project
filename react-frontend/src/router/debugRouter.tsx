/**
 * ============================================================================
 * 简化版路由配置文件 — 用于调试
 * ============================================================================
 */

import React from 'react'
import { createBrowserRouter, Navigate, Outlet, useLocation } from 'react-router-dom'
import { Layout, Menu, Button, message } from 'antd'
import {
  DashboardOutlined,
  CalendarOutlined,
  WalletOutlined,
  BookOutlined,
  EditOutlined,
  TrophyOutlined,
  UserOutlined,
  LogoutOutlined
} from '@ant-design/icons'
import LoginPage from '@/pages/login/LoginPage'
import RegisterPage from '@/pages/register/RegisterPage'
import DashboardPage from '@/pages/dashboard/DashboardPage'
import PlanPage from '@/pages/plan/PlanPage'
import AccountingPage from '@/pages/accounting/AccountingPage'
import ExcerptPage from '@/pages/excerpt/ExcerptPage'
import SummaryPage from '@/pages/summary/SummaryPage'
import GoalPage from '@/pages/goal/GoalPage'
import ProfilePage from '@/pages/profile/ProfilePage'
import TestPage from '@/pages/TestPage'
import { useUserStore } from '@/stores/useUserStore'

const { Header, Sider, Content } = Layout

/**
 * ProtectedRoute — 受保护的路由组件
 */
function ProtectedRoute({ children }: { children?: React.ReactNode }) {
  const isLoggedIn = useUserStore(state => state.isLoggedIn())
  const location = useLocation()

  if (!isLoggedIn) {
    message.warning('请先登录')
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
 * SimpleAppLayout — 简化的布局组件
 */
const SimpleAppLayout: React.FC = () => {
  const location = useLocation()

  // 菜单项配置
  const menuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '数据看板'
    },
    {
      key: '/plan',
      icon: <CalendarOutlined />,
      label: '每日计划'
    },
    {
      key: '/accounting',
      icon: <WalletOutlined />,
      label: '每日记账'
    },
    {
      key: '/excerpt',
      icon: <BookOutlined />,
      label: '每日摘录'
    },
    {
      key: '/summary',
      icon: <EditOutlined />,
      label: '每日总结'
    },
    {
      key: '/goal',
      icon: <TrophyOutlined />,
      label: '目标管理'
    },
    {
      key: '/profile',
      icon: <UserOutlined />,
      label: '个人中心'
    },
    {
      key: '/test',
      icon: <UserOutlined />,
      label: '测试页面'
    }
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        width={200}
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0
        }}
      >
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: 20,
            fontWeight: 'bold',
            background: '#001529'
          }}
        >
          DailyTracker
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onSelect={({ key }) => {
            console.log('Menu onSelect:', key)
            window.location.href = key
          }}
        />
      </Sider>
      <Layout style={{ marginLeft: 200 }}>
        <Header
          style={{
            padding: '0 24px',
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
          }}
        >
          <div style={{ fontWeight: 'bold', fontSize: 16 }}>
            {menuItems.find(item => item.key === location.pathname)?.label || 'Dashboard'}
          </div>
          <Button
            danger
            onClick={() => {
              useUserStore.getState().logout()
              window.location.href = '/login'
            }}
          >
            退出登录
          </Button>
        </Header>
        <Content
          style={{
            margin: '24px',
            padding: 24,
            background: '#fff',
            borderRadius: 8
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

/**
 * PageTitle — 页面标题更新组件
 */
function PageTitle() {
  const location = useLocation()

  React.useEffect(() => {
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
      <div style={{ textAlign: 'center' }}>
        <div>加载中...</div>
      </div>
    </div>
  )
}

/**
 * 创建路由器实例
 */
const router = createBrowserRouter([
  {
    path: '/login',
    element: (
      <PublicRoute>
        <PageTitle />
        <LoginPage />
      </PublicRoute>
    )
  },
  {
    path: '/register',
    element: (
      <PublicRoute>
        <PageTitle />
        <RegisterPage />
      </PublicRoute>
    )
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <PageTitle />
        <SimpleAppLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />
      },
      {
        path: 'dashboard',
        element: <DashboardPage />
      },
      {
        path: 'plan',
        element: <PlanPage />
      },
      {
        path: 'accounting',
        element: <AccountingPage />
      },
      {
        path: 'excerpt',
        element: <ExcerptPage />
      },
      {
        path: 'summary',
        element: <SummaryPage />
      },
      {
        path: 'goal',
        element: <GoalPage />
      },
      {
        path: 'profile',
        element: <ProfilePage />
      },
      {
        path: 'test',
        element: <TestPage />
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
