import React from 'react'
import { Empty } from 'antd'
import type { EmptyProps } from 'antd'

interface EmptyStateProps extends Omit<EmptyProps, 'image'> {
  description?: string
}

/**
 * EmptyState — 空状态提示组件
 */
const EmptyState: React.FC<EmptyStateProps> = ({ description = '暂无数据', ...props }) => {
  return <Empty description={description} {...props} />
}

export default EmptyState