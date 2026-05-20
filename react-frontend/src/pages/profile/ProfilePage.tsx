import React, { useState, useEffect } from 'react'
import { Avatar, Form, Input, Button, Card, message, Upload, Modal, Divider } from 'antd'
import { UserOutlined, UploadOutlined, LockOutlined, LogoutOutlined } from '@ant-design/icons'
import type { UploadProps } from 'antd'
import { userApi, type UserProfile } from '@/api/user'
import { useUserStore } from '@/stores/useUserStore'
import { useNavigate } from 'react-router-dom'

const { TextArea } = Input

/**
 * ProfilePage — 个人中心页面
 */
const ProfilePage: React.FC = () => {
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [loading, setLoading] = useState(false)
  const [passwordModalVisible, setPasswordModalVisible] = useState(false)
  const [passwordForm] = Form.useForm()
  const navigate = useNavigate()
  const { logout, updateUserInfo, avatar } = useUserStore()
  const avatarUrl = avatar()

  useEffect(() => {
    fetchProfile()
  }, [])

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

  const handleProfileUpdate = async (values: Partial<UserProfile>) => {
    try {
      await userApi.updateProfile(values)
      message.success('更新成功')
      fetchProfile()
      updateUserInfo(values)
    } catch (error) {
      console.error('更新失败:', error)
    }
  }

  const handlePasswordChange = async (values: { oldPassword: string; newPassword: string }) => {
    try {
      await userApi.changePassword(values)
      message.success('密码修改成功，请重新登录')
      setPasswordModalVisible(false)
      passwordForm.resetFields()
      logout()
      navigate('/login')
    } catch (error) {
      console.error('密码修改失败:', error)
    }
  }

  const handleAvatarUpload: UploadProps['customRequest'] = async (options) => {
    const { file, onSuccess, onError } = options
    try {
      const result = await userApi.updateAvatar(file as File)
      message.success('头像上传成功')
      updateUserInfo({ avatar: result.avatarUrl })
      fetchProfile()
      onSuccess?.(result)
    } catch (error) {
      console.error('头像上传失败:', error)
      onError?.(error as Error)
    }
  }

  const uploadProps: UploadProps = {
    name: 'avatar',
    customRequest: handleAvatarUpload,
    showUploadList: false,
    accept: 'image/*'
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <h2>个人中心</h2>

      {/* 头像卡片 */}
      <Card style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
          <Avatar size={100} src={avatarUrl || undefined} icon={!avatarUrl && <UserOutlined />} />
          <div>
            <h3>{profile?.nickname || profile?.username}</h3>
            <p style={{ color: '#999', margin: '8px 0' }}>
              用户名：{profile?.username}
            </p>
            <Upload {...uploadProps}>
              <Button icon={<UploadOutlined />}>更换头像</Button>
            </Upload>
          </div>
        </div>
      </Card>

      {/* 个人信息表单 */}
      <Card title="个人信息" style={{ marginBottom: 16 }}>
        <Form
          layout="vertical"
          initialValues={profile || undefined}
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
      </Card>

      {/* 账户安全 */}
      <Card title="账户安全" style={{ marginBottom: 16 }}>
        <Button
          icon={<LockOutlined />}
          onClick={() => setPasswordModalVisible(true)}
        >
          修改密码
        </Button>
      </Card>

      {/* 其他操作 */}
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

      {/* 修改密码弹窗 */}
      <Modal
        title="修改密码"
        open={passwordModalVisible}
        onCancel={() => {
          setPasswordModalVisible(false)
          passwordForm.resetFields()
        }}
        onOk={() => passwordForm.submit()}
      >
        <Form
          form={passwordForm}
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
