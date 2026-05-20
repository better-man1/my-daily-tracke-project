import React from 'react'
import { ConfigProvider } from 'antd'
import { RouterProvider } from 'react-router-dom'
import zhCN from 'antd/locale/zh_CN'
import ErrorBoundary from '@/components/common/ErrorBoundary'
import router from './router'
import './styles/global.css'

/**
 * App — 应用根组件
 */
const App: React.FC = () => {
  return (
    <ConfigProvider locale={zhCN}>
      <ErrorBoundary>
        <RouterProvider router={router} />
      </ErrorBoundary>
    </ConfigProvider>
  )
}

export default App
