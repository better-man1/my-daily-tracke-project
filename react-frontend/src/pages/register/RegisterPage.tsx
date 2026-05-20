import React, { useState } from 'react'
import { Form, Input, Button, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { authApi } from '@/api/auth'

/**
 * ============================================================================
 * 【注册页面组件 (src/pages/register/RegisterPage.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 【React 核心概念：跨字段表单验证 (Cross-Field Validation)】
 *    - 场景：在注册账户时，用户需要输入两次密码，且需要校验两次输入是否完全一致。
 *    - 挑战：当用户在“确认密码”框中输入内容时，校验函数如何获知“密码”框里输入了什么？
 *    - 解决方案（以 Antd Form 为例）：
 *      a. `dependencies={['password']}`：声明依赖项。告诉 Form 组件，一旦 `password` 字段的值变了，
 *         也必须自动重新触发对本字段（`confirmPassword`）的合法性验证。
 *      b. 使用函数式 Validator：`({ getFieldValue }) => ({ validator(_, value) { ... } })`。
 *         - 我们传入一个回调函数，Form 会注入一系列方法，其中最核心的是 `getFieldValue`（根据字段名读取当前值）。
 *         - 在内部，我们通过 `getFieldValue('password')` 实时取回主密码框的值，并与确认框的当前输入值 `value` 进行对比。
 *         - 如果一致，返回 `Promise.resolve()`（验证通过）；如果不一致，返回 `Promise.reject(new Error('...'))`（验证失败，展示错误文案）。
 *
 * 2. 什么是异步数据流与加载态 (Loading Flow)？
 *    - 注册是一个耗时长的网络 I/O 过程。
 *    - 我们在表单提交的那一刻将 `loading` 设为 `true`，以禁用注册按钮并显示旋转进度条。
 *    - 注册成功后，我们通过 `navigate('/login')` 触发路由跳转，将用户导向登录页。
 */
const RegisterPage: React.FC = () => {
  // 定义加载状态
  const [loading, setLoading] = useState(false)
  
  // 编程式导航 Hook
  const navigate = useNavigate()

  /**
   * 注册表单校验通过后的核心提交方法
   */
  const onFinish = async (values: { username: string; password: string; confirmPassword: string }) => {
    // 冗余的安全检查（防止表单组件绕过验证提交）
    if (values.password !== values.confirmPassword) {
      message.error('两次输入的密码不一致')
      return
    }

    try {
      // 开启按钮 loading
      setLoading(true)
      
      // 调用注册 API，向服务器发送注册账号请求
      await authApi.register({
        username: values.username,
        password: values.password
      })
      
      // 弹出轻提示
      message.success('注册成功，请登录')
      
      // 引导用户跳转到登录页面进行登录
      navigate('/login')
    } catch (error) {
      console.error('注册失败:', error)
    } finally {
      // 无论成功或失败，都重置 loading 状态
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
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }}
    >
      <Card
        title="注册"
        style={{ width: 400, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}
      >
        <Form
          name="register"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          {/* 用户名：支持必填校验和最小长度校验 */}
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名' },
              { min: 3, message: '用户名至少3个字符' }
            ]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名"
            />
          </Form.Item>

          {/* 密码：支持必填校验和最小长度校验 */}
          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 6, message: '密码至少6个字符' }
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          {/* 确认密码：带依赖关联的自定义验证规则 */}
          <Form.Item
            name="confirmPassword"
            dependencies={['password']} // 一旦 password 被修改，当前 confirmPassword 也会被重新校验
            rules={[
              { required: true, message: '请确认密码' },
              // 声明自定义校验函数
              ({ getFieldValue }) => ({
                validator(_, value) {
                  // 如果用户没填，或者输入的值与 password 的输入值完全一致，则通过验证
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve()
                  }
                  // 不一致则抛出错误对象，渲染至输入框下方
                  return Promise.reject(new Error('两次输入的密码不一致'))
                }
              })
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="确认密码"
            />
          </Form.Item>

          {/* 注册提交按钮 */}
          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>
              注册
            </Button>
          </Form.Item>

          {/* 返回登录链接 */}
          <Form.Item style={{ marginBottom: 0 }}>
            <div style={{ textAlign: 'center' }}>
              已有账号？<a onClick={() => navigate('/login')}>立即登录</a>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default RegisterPage