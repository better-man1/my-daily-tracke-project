import React from 'react'
import { Spin } from 'antd'

interface LoadingProps {
  description?: string
  tip?: string  // deprecated, use description
  size?: 'small' | 'default' | 'large'
}

/**
 * Loading — 加载指示器组件
 */
const Loading: React.FC<LoadingProps> = ({ description, tip = '加载中...', size = 'large' }) => {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100%',
        minHeight: 200
      }}
    >
      <Spin size={size} description={description || tip} />
    </div>
  )
}

export default Loading