import { Component, type ErrorInfo, type ReactNode } from 'react'
import { Result, Button } from 'antd'

/**
 * ErrorBoundaryProps — 定义属性接口
 * 接收 children 属性，类型为 ReactNode（表示任何合法的 React 渲染子元素）
 */
interface ErrorBoundaryProps {
  children: ReactNode
}

/**
 * ErrorBoundaryState — 定义组件内部状态
 */
interface ErrorBoundaryState {
  hasError: boolean        // 标识子组件树是否发生崩溃
  error: Error | null      // 缓存具体的错误对象信息
}

/**
 * ============================================================================
 * 【React 核心概念：错误边界类组件 (ErrorBoundary)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 为什么用类组件 (Class Component) 而不是函数组件 (Functional Component)？
 *    - 现代 React 倡导使用函数组件和 Hooks，但**截至当前，React Hooks 并没有提供能够捕获子组件渲染错误
 *      的 Hook**（如类似于 `useErrorBoundary` 的钩子）。
 *    - 编写“错误边界（Error Boundary）”是目前 React 生态中**唯一必须**使用类组件的场景。
 *
 * 2. 类组件的数据存储与初始化：
 *    - 类组件的数据通过继承自 `Component` 的 `state` 属性来声明，且必须在 `constructor`（构造函数）中
 *      通过 `this.state = { ... }` 赋予初始值。
 *    - 修改状态必须使用 `this.setState({ ... })`，而不能直接修改 `this.state`，这样 React 才能监听到数据变化并重绘。
 *
 * 3. 核心生命周期方法详解：
 *    - `static getDerivedStateFromError(error)`：
 *      - **触发时机**：当任何子孙组件在渲染（render）阶段、生命周期中抛出未捕获的 JavaScript 错误时。
 *      - **作用**：这是一个静态方法，必须返回一个新的状态对象用来更新组件的 `state`。在此处我们将 `hasError` 设为 `true`，
 *        React 就会在下一次渲染时使用新状态，从而展现“备用 UI”，避免白屏。
 *
 *    - `componentDidCatch(error, errorInfo)`：
 *      - **触发时机**：在错误被捕获后的“提交（commit）”阶段执行。
 *      - **作用**：常用于向外部错误监控平台（如 Sentry、LogRocket）发送错误日志，或本地控制台打印分析。
 *        它接收 `error`（错误对象）和 `errorInfo`（包含组件崩溃调用栈的对象）。
 *
 * 4. 容错和重试机制：
 *    - 在备用 UI 中提供“重试”按钮。点击后，调用 `this.setState({ hasError: false })` 将状态重置。
 *    - React 会重新尝试渲染子组件树（`this.props.children`）。如果引起崩溃的代码已经被修改，或异常只是偶发，系统即可恢复正常。
 */
class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    // 在子类的构造函数中，必须首先调用 super(props)，才能在后续使用 this 访问属性
    super(props)
    // 初始化类组件的状态
    this.state = {
      hasError: false,
      error: null
    }
  }

  // 渲染崩溃时的核心生命周期：用于更新 state 触发降级渲染
  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return {
      hasError: true,
      error
    }
  }

  // 渲染崩溃后的核心生命周期：用于记录和上报错误日志
  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    console.error('【错误边界捕获到未处理的崩溃】:', error, errorInfo)
  }

  // 重置错误状态，尝试恢复页面
  handleReset = (): void => {
    // 使用类组件专用的 setState 方法更新状态
    this.setState({
      hasError: false,
      error: null
    })
  }

  // 类组件的渲染入口：必须实现一个 render() 方法，并返回 ReactNode
  render(): ReactNode {
    // 如果子组件树已经崩溃，展示优雅的备用 UI (Antd Result)
    if (this.state.hasError) {
      return (
        <Result
          status="error"
          title="出错了"
          subTitle={this.state.error?.message || '应用程序遇到了一个运行错误'}
          extra={[
            <Button type="primary" key="retry" onClick={this.handleReset}>
              重试
            </Button>,
            <Button key="home" onClick={() => window.location.href = '/dashboard'}>
              返回首页
            </Button>
          ]}
        />
      )
    }

    // 若无错误，正常渲染传入的所有子组件
    return this.props.children
  }
}

export default ErrorBoundary
