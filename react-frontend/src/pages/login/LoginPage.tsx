import React, { useState } from 'react'
// Form：表单容器。Input：输入框。Button：按钮。Card：卡片容器。message：全局轻提示
import { Form, Input, Button, Card, message } from 'antd'
// 导入文本输入框前置修饰图标
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/useUserStore'

/**
 * ============================================================================
 * 【登录页面组件 (src/pages/login/LoginPage.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 【React 核心概念：本地状态与 UI 绑定 (useState)】
 *    - `const [loading, setLoading] = useState(false)`：声明一个名为 `loading` 的状态。
 *    - 当点击登录按钮执行异步 API 请求时，我们通过 `setLoading(true)` 让按钮处于加载旋转状态。
 *    - 请求结束后，无论成功或失败，都在 `finally` 块中通过 `setLoading(false)` 关闭加载态。
 *    - 这是 React 控制 UI 表现的最核心模式：数据（State）决定视图（View）。
 *
 * 2. 【React 核心概念：受控与非受控表单 (Form Handling)】
 *    - 在传统 HTML 开发中，我们需要手动获取 input 的 DOM 节点并读取其 `.value`。
 *    - 在 React 中，我们常用“受控组件”模式或利用组件库封装好的表单状态管理器。
 *    - 这里使用 Antd 的 `<Form>` 组件：它是一个高性能的表单状态管理器。
 *      - 不需要为每个输入框写额外的 `onChange` 监听器，Form 会自动接管表单状态。
 *      - 当用户点击提交且所有验证通过时，Form 会自动收集数据，并以 `{ username, password }` 的对象形式
 *        传给我们绑定的 `onFinish` 回调函数。
 *
 * 3. 校验规则 (Validation Rules)：
 *    - `<Form.Item>` 上的 `rules` 属性用于声明验证规则。例如 `required: true` 代表必填。
 *    - 表单验证是由 Antd 内部的 `async-validator` 引擎处理的，它会阻止未填写的表单向后端发送请求。
 */
const LoginPage: React.FC = () => {
  // 定义加载状态，用于控制登录按钮的 loading 样式
  const [loading, setLoading] = useState(false)
  
  // 路由跳转导航方法
  const navigate = useNavigate()
  
  // 获取全局 Zustand Store 中保存登录信息的 Actions 方法
  const setLoginData = useUserStore(state => state.setLoginData)

  /**
   * 表单验证成功并通过后的提交回调函数
   * @param values 表单自动收集的所有输入数据对象，如 { username: 'xxx', password: 'yyy' }
   */
  const onFinish = async (values: { username: string; password: string }) => {
    try {
      // 1. 开启按钮加载状态，防止用户重复点击，引发多次重复登录请求
      setLoading(true)
      
      // 2. 调用 API 登录接口，向后端发起网络请求
      const data = await authApi.login(values)
      
      // 3. 将后端返回的用户资料、JWT Token 保存到全局 Zustand 状态中
      setLoginData(data)
      
      // 4. 显示操作成功提示
      message.success('登录成功')
      
      // 5. 跳转到主控台数据看板页面
      navigate('/dashboard')
    } catch (error) {
      // 异常由网络请求拦截器统一捕获并给出轻提示，这里仅在控制台打印调试
      console.error('登录失败:', error)
    } finally {
      // 6. 无论登录成功或失败，都必须关闭加载状态
      setLoading(false)
    }
  }

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        // 背景使用渐变色，提升视觉高级感
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }}
    >
      {/* 登录卡片 */}
      <Card
        title="登录"
        style={{ width: 400, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}
      >
        <Form
          name="login"
          onFinish={onFinish} // 绑定验证通过后的提交逻辑
          autoComplete="off"  // 关闭浏览器的输入框历史填充
          size="large"        // 表单尺寸设为大尺寸
        >
          {/* 用户名输入项 */}
          <Form.Item
            name="username" // 对应 values.username
            rules={[{ required: true, message: '请输入用户名' }]} // 声明必填验证
          >
            <Input
              prefix={<UserOutlined />} // 前置图标
              placeholder="用户名"
            />
          </Form.Item>

          {/* 密码输入项 */}
          <Form.Item
            name="password" // 对应 values.password
            rules={[{ required: true, message: '请输入密码' }]}
          >
            {/* Input.Password 组件会自动提供密码的“明文/暗文”眼睛切换功能 */}
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          {/* 登录提交按钮 */}
          <Form.Item>
            {/* htmlType="submit" 代表点击该按钮会触发 Form 的提交与验证流程 */}
            <Button type="primary" htmlType="submit" block loading={loading}>
              登录
            </Button>
          </Form.Item>

          {/* 跳转注册链接项 */}
          <Form.Item style={{ marginBottom: 0 }}>
            <div style={{ textAlign: 'center' }}>
              还没有账号？<a onClick={() => navigate('/register')}>立即注册</a>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default LoginPage