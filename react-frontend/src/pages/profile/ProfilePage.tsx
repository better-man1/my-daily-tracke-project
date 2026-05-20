import React, { useState, useEffect } from 'react'
import { Avatar, Form, Input, Button, Card, message, Upload, Modal } from 'antd'
import { UserOutlined, UploadOutlined, LockOutlined, LogoutOutlined } from '@ant-design/icons'
import type { UploadProps } from 'antd'
import { userApi, type UserProfile } from '@/api/user'
import { useUserStore } from '@/stores/useUserStore'
import { useNavigate } from 'react-router-dom'

const { TextArea } = Input

/**
 * ============================================================================
 * 【个人中心页面组件 (src/pages/profile/ProfilePage.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 【React 核心概念：Form 实例操纵器 (Form.useForm)】
 *    - 当我们需要在表单“外部”控制表单行为（例如：在 Modal 的 `onOk` 回调函数中触发表单提交，
 *      或者提交成功后重置表单输入框），就必须使用 `Form.useForm()`。
 *    - `const [passwordForm] = Form.useForm()` 会返回一个 Form 实例对象。
 *    - 我们将实例绑定到 `<Form form={passwordForm}>` 上，之后即可使用：
 *      - `passwordForm.submit()`：触发提交验证流程。
 *      - `passwordForm.resetFields()`：清空表单所有字段。
 *
 * 2. 挂载时只获取一次数据 (componentDidMount 等价写法)：
 *    - `useEffect(() => { fetchProfile() }, [])`
 *    - 第二个参数是空数组 `[]`，意味着这个副作用函数没有任何外部依赖。
 *    - 这样，回调函数**只会在组件第一次挂载到页面上时运行一次**，等同于类组件的 `componentDidMount` 生命周期。
 *
 * 3. 状态同步机制 (State Synchronization)：
 *    - 用户修改了个人资料（如昵称、头像）并向后端保存成功后，我们做了两步：
 *      a. `fetchProfile()`：重新获取本页面的本地 `profile` 状态，更新页面输入框展示。
 *      b. `updateUserInfo(values)`：调用全局 Zustand Store 的 Action 更新全局状态。
 *      - 这样一来，顶部 Header 的 Avatar 和昵称也会**立即同步变动**，而不需要用户刷新整个页面。
 *
 * 4. 【异步文件上传 (Custom File Upload in SPA)】
 *    - 使用 Antd 的 Upload 组件时，如果不配置 action 属性，它会默认发起 HTTP POST 请求。
 *    - 更好的方案是使用 `customRequest` 接管文件上传流程，我们可以直接拿到原始的 `File` 对象。
 *    - 在 `handleAvatarUpload` 中，我们构建 multipart 请求调用 `userApi.updateAvatar`，
 *      上传成功后读取返回的新头像 URL 并同步更新 Zustand Global State。
 */
const ProfilePage: React.FC = () => {
  // 本地组件状态：存储从 API 获取的个人资料详情
  const [profile, setProfile] = useState<UserProfile | null>(null)
  // 保存动作的 loading 状态
  const [loading, setLoading] = useState(false)
  // 控制“修改密码”弹窗 Modal 是否可见的状态
  const [passwordModalVisible, setPasswordModalVisible] = useState(false)
  
  // 创建修改密码表单的控制实例
  const [passwordForm] = Form.useForm()
  
  const navigate = useNavigate()
  
  // 从全局 Zustand Store 获取属性和方法
  const { logout, updateUserInfo, avatar } = useUserStore()
  // Zustand getter 是函数，需要调用以获取当前的值
  const avatarUrl = avatar()

  // 挂载时获取数据
  useEffect(() => {
    fetchProfile()
  }, [])

  /**
   * 从后端拉取当前登录用户的详细资料
   */
  const fetchProfile = async () => {
    try {
      setLoading(true)
      const data = await userApi.getProfile()
      setProfile(data)
    } catch (error) {
      console.error('获取个人信息失败:', error)
    } finally {
      setLoading(false)
    }
  }

  /**
   * 更新个人基本信息（昵称、邮箱、手机号等）
   */
  const handleProfileUpdate = async (values: Partial<UserProfile>) => {
    try {
      // 1. 发起 API 请求更新服务器数据库中的信息
      await userApi.updateProfile(values)
      message.success('更新成功')
      // 2. 重新拉取最新数据刷新页面
      fetchProfile()
      // 3. 将更新同步至全局 Store（同步修改 Header 等区域展示的用户数据）
      updateUserInfo(values)
    } catch (error) {
      console.error('更新失败:', error)
    }
  }

  /**
   * 修改密码提交回调
   */
  const handlePasswordChange = async (values: { oldPassword: string; newPassword: string }) => {
    try {
      // 1. 发起修改密码请求
      await userApi.changePassword(values)
      message.success('密码修改成功，请重新登录')
      // 2. 关闭 Modal 弹窗
      setPasswordModalVisible(false)
      // 3. 重置并清空密码弹窗中的所有输入框内容
      passwordForm.resetFields()
      // 4. 清理登录状态（Token 失效），引导用户跳转至登录页重新核验
      logout()
      navigate('/login')
    } catch (error) {
      console.error('密码修改失败:', error)
    }
  }

  /**
   * 自定义头像上传处理方法
   */
  const handleAvatarUpload: UploadProps['customRequest'] = async (options) => {
    const { file, onSuccess, onError } = options
    try {
      // file 默认为 UploadFile 类型，需要断言为 JS 原生的 File 对象进行文件上传
      const result = await userApi.updateAvatar(file as File)
      message.success('头像上传成功')
      
      // 更新全局 Zustand Store 中的用户头像，顶部导航条的头像会响应式更新为新头像
      updateUserInfo({ avatar: result.avatarUrl })
      // 刷新本地 Profile 接口状态
      fetchProfile()
      // 通知 Antd Upload 组件文件上传成功
      onSuccess?.(result)
    } catch (error) {
      console.error('头像上传失败:', error)
      onError?.(error as Error)
    }
  }

  // 配置上传组件的参数
  const uploadProps: UploadProps = {
    name: 'avatar',
    customRequest: handleAvatarUpload, // 注入自定义的上传处理
    showUploadList: false,             // 不展示上传列表文件信息
    accept: 'image/*'                  // 限制只能选择图片类型文件
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <h2>个人中心</h2>

      {/* 头像卡片 */}
      <Card style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
          {/* Avatar 头像渲染，若没有头像 URL，则显示 UserOutlined 默认图标 */}
          <Avatar size={100} src={avatarUrl || undefined} icon={!avatarUrl && <UserOutlined />} />
          <div>
            <h3>{profile?.nickname || profile?.username}</h3>
            <p style={{ color: '#999', margin: '8px 0' }}>
              用户名：{profile?.username}
            </p>
            {/* 上传包裹按钮 */}
            <Upload {...uploadProps}>
              <Button icon={<UploadOutlined />}>更换头像</Button>
            </Upload>
          </div>
        </div>
      </Card>

      {/* 个人信息表单 */}
      <Card title="个人信息" style={{ marginBottom: 16 }}>
        {/*
          React 核心细节：表单回显
          由于 profile 数据是异步拉取的，初始化时可能为 null。
          我们必须在 Form 上设置 key={profile?.userId || 'loading'} 或者是使用其内置的 initialValues。
          当 profile 状态从 null 变为有效数据时，Form 组件如果使用了 initialValues，
          可以配合在每次更新后自动同步展示，或是手动设置。
          此处 initialValues 传入 profile || undefined，当 profile 加载完毕后，Form 会自动把
          含有 name="nickname" 等字段的值填充进输入框内。
        */}
        {profile && (
          <Form
            layout="vertical"
            initialValues={profile}
            onFinish={handleProfileUpdate}
          >
            <Form.Item
              name="nickname"
              label="昵称"
              rules={[{ required: true, message: '请输入昵称' }]}
            >
              <Input placeholder="请输入昵称" />
            </Form.Item>

            <Form.Item name="email" label="邮箱">
              <Input placeholder="请输入邮箱" />
            </Form.Item>

            <Form.Item name="phone" label="手机号">
              <Input placeholder="请输入手机号" />
            </Form.Item>

            <Form.Item name="signature" label="个人签名">
              <TextArea rows={3} placeholder="请输入个人签名" />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading}>
                保存修改
              </Button>
            </Form.Item>
          </Form>
        )}
      </Card>

      {/* 账户安全卡片 */}
      <Card title="账户安全" style={{ marginBottom: 16 }}>
        <Button
          icon={<LockOutlined />}
          onClick={() => setPasswordModalVisible(true)} // 打开修改密码的 Modal 弹窗
        >
          修改密码
        </Button>
      </Card>

      {/* 退出登录选项 */}
      <Card>
        <Button
          danger
          icon={<LogoutOutlined />}
          onClick={() => {
            logout()
            navigate('/login')
          }}
        >
          退出登录
        </Button>
      </Card>

      {/* 异步控制的修改密码弹窗 */}
      <Modal
        title="修改密码"
        open={passwordModalVisible}
        // 点击取消或右上角关闭时，不仅隐藏 Modal，同时清空里面的输入框痕迹
        onCancel={() => {
          setPasswordModalVisible(false)
          passwordForm.resetFields()
        }}
        // 点击确定按钮时，调用 passwordForm.submit()，让表单开始独立执行 validate 和 onFinish 逻辑
        onOk={() => passwordForm.submit()}
      >
        <Form
          form={passwordForm} // 绑定实例，以便在 Modal 外部通过 passwordForm 实例操纵此表单
          layout="vertical"
          onFinish={handlePasswordChange}
        >
          <Form.Item
            name="oldPassword"
            label="原密码"
            rules={[{ required: true, message: '请输入原密码' }]}
          >
            <Input.Password placeholder="请输入原密码" />
          </Form.Item>

          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '密码至少6个字符' }
            ]}
          >
            <Input.Password placeholder="请输入新密码" />
          </Form.Item>

          {/* 确认新密码：需要与 newPassword 保持一致，通过 validator 进行依赖读取 */}
          <Form.Item
            name="confirmPassword"
            label="确认密码"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve()
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'))
                }
              })
            ]}
          >
            <Input.Password placeholder="请确认密码" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ProfilePage
