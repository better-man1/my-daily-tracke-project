import React from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Button, Card, Space } from 'antd'

/**
 * 测试路由的页面
 */
const TestPage: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()

  console.log('TestPage rendered at:', location.pathname)

  const routes = [
    '/dashboard',
    '/plan',
    '/accounting',
    '/excerpt',
    '/summary',
    '/goal',
    '/profile'
  ]

  return (
    <div style={{ padding: '24px' }}>
      <Card title="路由测试页面">
        <p>当前路径: <strong>{location.pathname}</strong></p>
        <p>点击下方按钮测试导航功能：</p>
        <Space wrap>
          {routes.map(route => (
            <Button
              key={route}
              type={location.pathname === route ? 'primary' : 'default'}
              onClick={() => {
                console.log('Navigating to:', route)
                navigate(route)
              }}
            >
              {route}
            </Button>
          ))}
        </Space>
      </Card>
    </div>
  )
}

export default TestPage