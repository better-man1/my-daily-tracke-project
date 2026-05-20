import React, { useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Avatar, Dropdown, theme, Button } from 'antd'
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
 * AppLayout — 应用主布局组件
 *
 * 包含侧边栏、顶部导航和内容区域。
 */
const AppLayout: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { token } = theme.useToken()
  const nickname = useUserStore(state => state.nickname())
  const avatar = useUserStore(state => state.avatar())
  const logout = useUserStore(state => state.logout)

  const [collapsed, setCollapsed] = useState(false)

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
    }
  ]

  // 处理菜单点击
  const handleMenuClick = (key: string) => {
    console.log('Menu clicked:', key)
    navigate(key)
  }

  // 用户下拉菜单
  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
      onClick: () => {
        console.log('Navigate to profile')
        navigate('/profile')
      }
    },
    {
      type: 'divider' as const
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        console.log('Logout')
        logout()
        navigate('/login')
      }
    }
  ]

  // 当前选中的菜单项（精确匹配路径）
  const getSelectedKey = () => {
    // 如果在子路由中，返回父路由
    const path = location.pathname
    if (path.startsWith('/plan')) return '/plan'
    if (path.startsWith('/accounting')) return '/accounting'
    if (path.startsWith('/excerpt')) return '/excerpt'
    if (path.startsWith('/summary')) return '/summary'
    if (path.startsWith('/goal')) return '/goal'
    if (path.startsWith('/profile')) return '/profile'
    return path
  }

  console.log('Current location:', location.pathname, 'Selected key:', getSelectedKey())

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
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
            color: token.colorWhite,
            fontSize: collapsed ? 20 : 24,
            fontWeight: 'bold'
          }}
        >
          {collapsed ? 'DT' : 'DailyTracker'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[getSelectedKey()]}
          items={menuItems}
          onSelect={({ key }) => {
            console.log('Menu item selected:', key)
            navigate(key)
          }}
        />
      </Sider>
      <Layout style={{ marginLeft: collapsed ? 80 : 200, transition: 'margin-left 0.2s' }}>
        <Header
          style={{
            padding: '0 24px',
            background: token.colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}
        >
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
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Avatar src={avatar || undefined} icon={!avatar && <UserOutlined />} />
              <span>{nickname}</span>
            </div>
          </Dropdown>
        </Header>
        <Content
          style={{
            margin: '24px',
            padding: 24,
            background: token.colorBgContainer,
            borderRadius: token.borderRadiusLG
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default AppLayout