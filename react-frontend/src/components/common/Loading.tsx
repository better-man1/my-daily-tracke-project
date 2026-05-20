import React from 'react'
import { Spin } from 'antd'

/**
 * LoadingProps — 声明组件接收的属性 (Props) 类型
 */
interface LoadingProps {
  description?: string                  // 加载描述文案（可选）
  tip?: string                          // 备用文案（可选，已废弃，建议用 description）
  size?: 'small' | 'default' | 'large'  // 指示器尺寸大小（可选）
}

/**
 * ============================================================================
 * 【React 核心概念：Props 解构与内联样式 (Loading Component)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. Props（属性）与解构默认值：
 *    - React 组件就像一个纯函数，它接收一个参数叫 `props`（包含外部传入的所有配置数据），
 *      并返回渲染出的 JSX。
 *    - 我们常使用 ES6 的解构赋值直接在参数中提取属性：`({ description, tip = '加载中...', size = 'large' })`。
 *    - 可以在解构时为可选属性赋予“默认值”（例如 `tip = '加载中...'`），当父组件没有传递该属性时，就会使用默认值。
 *
 * 2. React 中的内联样式 (Inline Styles)：
 *    - 在 HTML 中，样式是普通的 CSS 字符串：`style="display: flex; align-items: center;"`。
 *    - 在 React JSX 中，`style` 必须传入一个 **JavaScript 对象**：`style={{ display: 'flex', alignItems: 'center' }}`。
 *      - 外层大括号 `{}` 表示这里要写 JavaScript 表达式。
 *      - 内层大括号 `{}` 表示这是一个 JS 对象字面量。
 *      - **命名规范**：CSS 属性名必须采用【驼峰命名法 (camelCase)】。例如 `align-items` 变成 `alignItems`，
 *        `min-height` 变成 `minHeight`。属性值必须是用引号包裹的字符串。
 */
const Loading: React.FC<LoadingProps> = ({ description, tip = '加载中...', size = 'large' }) => {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100%',
        minHeight: 200 // 在 React 中，像 minHeight 等数值类型的 CSS 属性，可以直接写数字，React 会自动帮我们加上 'px' 单位
      }}
    >
      {/* 渲染 Ant Design 的 Spin（加载中旋转效果）组件 */}
      <Spin size={size} description={description || tip} />
    </div>
  )
}

export default Loading