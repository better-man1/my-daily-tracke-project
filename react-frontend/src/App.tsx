import React from 'react'
// 导入 Ant Design 的全局配置组件 ConfigProvider，这是 React Context（上下文）设计模式的典型应用
import { ConfigProvider } from 'antd'
// 导入 React Router 的路由提供者组件，用于在全局提供路由能力
import { RouterProvider } from 'react-router-dom'
// 导入 Ant Design 的中文语言包，用于汉化组件（如日期选择器、表格分页等）
import zhCN from 'antd/locale/zh_CN'
// 导入自定义的错误边界组件，用于捕获整个应用的运行崩溃异常
import ErrorBoundary from '@/components/common/ErrorBoundary'
// 导入预设好的路由配置对象
import router from './router'
// 导入全局样式文件
import './styles/global.css'

/**
 * ============================================================================
 * 【React 根组件 (App.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 什么是函数式组件 (Function Component)？
 *    - 现代 React 推荐使用函数来编写组件：一个返回 JSX 的 JavaScript 函数就是一个组件。
 *    - `React.FC` 是 TypeScript 中 `React.FunctionComponent` 的缩写类型，它声明了这是一个
 *      标准的 React 函数组件，会自动包含对 `children` 等属性的类型支持。
 *
 * 2. 【React 核心概念：Context Provider 模式】
 *    - 在 React 中，默认只能通过父传子 (Props) 的方式向下传递数据。当多层级组件都需要同一份数据
 *      时（如主题颜色、语言环境），逐级传递非常繁琐，这被称为 "Prop Drilling"。
 *    - React 提供了 Context（上下文）机制，允许数据跨越组件树直接传递。
 *    - `<ConfigProvider locale={zhCN}>` 就是一个 Provider（提供者），它利用 Context 将“中文语言包”
 *      隐式广播给子树中所有的 Ant Design 组件，任何子孙组件（如 DatePicker）都可以直接消费它，
 *      无需我们手动传参。
 *
 * 3. 为什么错误边界 `<ErrorBoundary>` 要包裹在 `<RouterProvider>` 外层？
 *    - React 组件渲染过程中如果抛出异常且未被捕获，会导致整个组件树崩溃，页面变白屏。
 *    - 错误边界是一个特殊的类组件，能捕获其【子组件树】中渲染阶段的错误。
 *    - 将它套在路由提供者外层，可以确保当任何路由页面（如 Dashboard、Plan）发生严重崩溃时，
 *      错误不会向上传递导致整个页面白屏，而是优雅地显示“出错啦”的备用 UI。
 *
 * 4. `<RouterProvider router={router} />` 是什么？
 *    - 这是 React Router 6.x 的全新写法。它通过向子组件广播路由状态，让应用可以根据当前浏览器的
 *      URL 路径，动态决定在 `<Outlet />` 占位处渲染哪个页面组件。
 */
const App: React.FC = () => {
  return (
    // 使用 Antd 的配置提供者，将应用的语言环境全局设置为中文
    <ConfigProvider locale={zhCN}>
      {/* 捕获整个应用的错误，避免白屏 */}
      <ErrorBoundary>
        {/* 注入路由配置，接管页面的渲染和跳转 */}
        <RouterProvider router={router} />
      </ErrorBoundary>
    </ConfigProvider>
  )
}

export default App

