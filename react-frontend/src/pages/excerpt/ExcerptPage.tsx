import React, { useState, useEffect } from 'react'
import { PlusOutlined, EditOutlined, DeleteOutlined, StarOutlined, StarFilled } from '@ant-design/icons'
import { Table, Button, Modal, Form, Input, Select, Space, message, Popconfirm, Card, Input as AntInput, Tag } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { excerptApi, type ExcerptItem, type CreateExcerptRequest } from '@/api/excerpt'

const { TextArea, Search } = AntInput
const { Option } = Select

/**
 * ExcerptPage — 每日摘录页面
 */
const ExcerptPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [excerpts, setExcerpts] = useState<ExcerptItem[]>([])
  const [modalVisible, setModalVisible] = useState(false)
  const [editingExcerpt, setEditingExcerpt] = useState<ExcerptItem | null>(null)
  const [form] = Form.useForm()
  const [searchKeyword, setSearchKeyword] = useState('')
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 })

  useEffect(() => {
    fetchExcerpts()
  }, [pagination.current, pagination.pageSize, searchKeyword])

  const fetchExcerpts = async () => {
    try {
      setLoading(true)
      const data = await excerptApi.list({
        page: pagination.current,
        pageSize: pagination.pageSize,
        keyword: searchKeyword
      })
      setExcerpts(data.list)
      setPagination({ ...pagination, total: data.total })
    } catch (error) {
      console.error('获取摘录失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditingExcerpt(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record: ExcerptItem) => {
    setEditingExcerpt(record)
    form.setFieldsValue(record)
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await excerptApi.delete(id)
      message.success('删除成功')
      fetchExcerpts()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }

  const handleToggleFavorite = async (record: ExcerptItem) => {
    try {
      await excerptApi.toggleFavorite(record.id)
      message.success(record.isFavorite ? '已取消收藏' : '已收藏')
      fetchExcerpts()
    } catch (error) {
      console.error('操作失败:', error)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const data: CreateExcerptRequest = values

      if (editingExcerpt) {
        await excerptApi.update(editingExcerpt.id, data)
        message.success('更新成功')
      } else {
        await excerptApi.create(data)
        message.success('创建成功')
      }

      setModalVisible(false)
      fetchExcerpts()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  const columns: ColumnsType<ExcerptItem> = [
    {
      title: '内容',
      dataIndex: 'content',
      key: 'content',
      ellipsis: true,
      render: (text) => <div style={{ maxWidth: 300 }}>{text}</div>
    },
    {
      title: '来源',
      key: 'source',
      width: 150,
      render: (_, record) => (
        <div>
          <div>{record.sourceTitle || '-'}</div>
          {record.author && <div style={{ fontSize: 12, color: '#999' }}>{record.author}</div>}
        </div>
      )
    },
    {
      title: '标签',
      dataIndex: 'tags',
      key: 'tags',
      width: 200,
      render: (tags: NonNullable<ExcerptItem['tags']>) => (
        <>
          {tags?.map(tag => (
            <Tag key={tag.id} color={tag.color}>{tag.name}</Tag>
          ))}
        </>
      )
    },
    {
      title: '收藏',
      dataIndex: 'isFavorite',
      key: 'isFavorite',
      width: 80,
      render: (isFavorite, record) => (
        <Button
          type="text"
          icon={isFavorite ? <StarFilled style={{ color: '#faad14' }} /> : <StarOutlined />}
          onClick={() => handleToggleFavorite(record)}
        />
      )
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          />
          <Popconfirm
            title="确定删除？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>每日摘录</h2>
        <Space>
          <Search
            placeholder="搜索摘录内容"
            onSearch={setSearchKeyword}
            style={{ width: 300 }}
            allowClear
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建摘录
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={excerpts}
        rowKey="id"
        loading={loading}
        pagination={{
          ...pagination,
          onChange: (page, pageSize) => setPagination({ current: page, pageSize: pageSize || 10, total: pagination.total })
        }}
      />

      <Modal
        title={editingExcerpt ? '编辑摘录' : '新建摘录'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="content"
            label="摘录内容"
            rules={[{ required: true, message: '请输入摘录内容' }]}
          >
            <TextArea rows={4} placeholder="请输入摘录内容" />
          </Form.Item>

          <Form.Item name="thought" label="个人思考">
            <TextArea rows={3} placeholder="请输入个人思考" />
          </Form.Item>

          <Form.Item name="sourceType" label="来源类型">
            <Select placeholder="请选择来源类型" allowClear>
              <Option value="book">书籍</Option>
              <Option value="article">文章</Option>
              <Option value="video">视频</Option>
              <Option value="other">其他</Option>
            </Select>
          </Form.Item>

          <Form.Item name="sourceTitle" label="来源标题">
            <Input placeholder="请输入来源标题" />
          </Form.Item>

          <Form.Item name="author" label="作者">
            <Input placeholder="请输入作者" />
          </Form.Item>

          <Form.Item name="page" label="页码">
            <Input placeholder="请输入页码" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ExcerptPage
