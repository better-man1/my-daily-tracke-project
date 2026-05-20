import React, { useState } from 'react'
// Outlet：嵌套路由渲染的出口。useNavigate：跳转路由的编程式导航方法。useLocation：获取当前 URL 路径信息。
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
// 引入 Antd 的布局和常用组件
import { Layout, Menu, Avatar, Dropdown, theme, Button } from 'antd'
// 引入侧边栏导航所需的全部图标
import {
  DashboardOutlined,
  CalendarOutlined,
  WalletOutlined,
  BookOutlined,
  EditOutlined,
  TrophyOutlined,
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined
} from '@ant-design/icons'
import { useUserStore } from '@/stores/useUserStore'

const { Header, Sider, Content } = Layout

/**
 * ============================================================================
 * 【应用全局布局组件 (src/components/layout/AppLayout.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 【React 核心概念：嵌套路由与 <Outlet />】
 *    - 许多管理后台项目在不同页面间共享同一套导航和侧边栏（Layout）。
 *    - 在 `router/index.tsx` 中，我们将 `AppLayout` 设为父级路由，把具体的页面（Dashboard, Plan 等）设为 `children`（子路由）。
 *    - 布局组件内部放置的 `<Outlet />`，就是这些子路由对应页面渲染的“占位插槽”。当从“/dashboard”切换到“/plan”时，
 *      侧边栏和头部不会重新挂载或闪烁，只有 `<Outlet />` 部分会被更新为对应的页面，这极大提升了用户体验。
 *
 * 2. 编程式路由跳转 (`useNavigate`)：
 *    - React 中，声明式跳转使用 `<Link to="/path">`。
 *    - 若要在 JavaScript 逻辑中触发跳转（例如点击下拉菜单项、登出后清理状态再跳转），应使用 React Router 提供的 `useNavigate` 钩子。
 *      - 调用 `navigate('/login')` 即可实现路径切换。
 *
 * 3. 响应式布局与局部状态更新：
 *    - `const [collapsed, setCollapsed] = useState(false)`：声明侧边栏收起状态。
 *    - 侧边栏的展开/折叠状态是由 React 组件内部状态驱动的。当点击展开/收起按钮时，调用 `setCollapsed(!collapsed)`。
 *    - React 监听到 `collapsed` 的值改变，会自动重新渲染 `AppLayout` 组件，从而动态改变 `<Sider>` 的宽度，以及右侧内容区的缩进。
 *
 * 4. Antd 主题 Token 读取 (`theme.useToken`)：
 *    - Antd 允许我们动态读取当前主题配置中的色彩、圆角等变量。
 *    - 通过 `const { token } = theme.useToken()`，我们可以在内联样式中直接使用 `token.colorBgContainer`（容器背景色）、
 *      `token.borderRadiusLG`（大圆角）等，保证页面风格与 UI 库主题的绝对统一。
 */
const AppLayout: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  
  // 读取 Antd 全局主题配置，用于编写保持一致的 inline 样式
  const { token } = theme.useToken()
  
  // 从全局 Zustand 状态管理库中订阅用户昵称、头像和登出方法
  const nickname = useUserStore(state => state.nickname())
  const avatar = useUserStore(state => state.avatar())
  const logout = useUserStore(state => state.logout)

  // 声明侧边栏折叠状态：默认不折叠 (false)
  const [collapsed, setCollapsed] = useState(false)

  // 定义侧边栏导航项的数据结构，key 值直接对应目标路由路径
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
    }
  ]

  // 定义右上角用户头像下拉菜单的内容
  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
      onClick: () => {
        navigate('/profile')
      }
    },
    {
      type: 'divider' as const // 菜单分割线
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        // 1. 调用 Zustand Action：擦除全局状态及本地存储的 token
        logout()
        // 2. 编程式跳转回登录页，且不能被回退
        navigate('/login', { replace: true })
      }
    }
  ]

  /**
   * 5. 计算当前高亮的菜单项：
   *    - 默认情况下，当用户访问子路径（如 `/plan/calendar` 或编辑页），直接匹配 `location.pathname` 可能会导致侧边栏没有选中项。
   *    - 这里通过前缀匹配，确保当用户在某个大模块的子页面时，侧边栏对应的主分类依然能够保持高亮选中状态。
   */
  const getSelectedKey = () => {
    const path = location.pathname
    if (path.startsWith('/plan')) return '/plan'
    if (path.startsWith('/accounting')) return '/accounting'
    if (path.startsWith('/excerpt')) return '/excerpt'
    if (path.startsWith('/summary')) return '/summary'
    if (path.startsWith('/goal')) return '/goal'
    if (path.startsWith('/profile')) return '/profile'
    return path
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* 侧边栏布局 */}
      <Sider
        trigger={null} // 隐藏默认的底部折叠触发器，改用 Header 里的自定义按钮
        collapsible    // 开启折叠功能支持
        collapsed={collapsed} // 由状态 collapsed 决定当前是否折叠
        onCollapse={setCollapsed}
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed', // 固定侧边栏在最左侧
          left: 0,
          top: 0,
          bottom: 0
        }}
      >
        {/* Logo 区域 */}
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: token.colorWhite,
            fontSize: collapsed ? 20 : 24,
            fontWeight: 'bold'
          }}
        >
          {collapsed ? 'DT' : 'DailyTracker'}
        </div>
        {/* 侧边栏导航菜单组件 */}
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[getSelectedKey()]} // 设置当前高亮项
          items={menuItems}
          onSelect={({ key }) => {
            // 点击菜单项时，调用 navigate 跳转到 key（即对应的路由路径）
            navigate(key)
          }}
        />
      </Sider>

      {/* 右侧主布局区域，其左外边距需要根据侧边栏是否折叠而动态调整 */}
      <Layout style={{ marginLeft: collapsed ? 80 : 200, transition: 'margin-left 0.2s' }}>
        {/* 顶部通栏 Header */}
        <Header
          style={{
            padding: '0 24px',
            background: token.colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}
        >
          {/* 折叠开关按钮：点击时切换 collapsed 状态 */}
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{
              fontSize: '16px',
              width: 64,
              height: 64
            }}
          />
          {/* 用户下拉菜单交互 */}
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
              {/* 若用户无头像，则降级显示默认 User 图标 */}
              <Avatar src={avatar || undefined} icon={!avatar && <UserOutlined />} />
              <span>{nickname}</span>
            </div>
          </Dropdown>
        </Header>

        {/* 核心内容展示区域 Content */}
        <Content
          style={{
            margin: '24px',
            padding: 24,
            background: token.colorBgContainer,
            borderRadius: token.borderRadiusLG
          }}
        >
          {/* 嵌套路由的真正子页面渲染点：子页面在这里被渲染和替换 */}
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default AppLayout