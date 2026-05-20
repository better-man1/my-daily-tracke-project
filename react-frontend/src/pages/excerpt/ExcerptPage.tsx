import React, { useState, useEffect } from 'react'
import { PlusOutlined, EditOutlined, DeleteOutlined, StarOutlined, StarFilled } from '@ant-design/icons'
import { Table, Button, Modal, Form, Input, Select, Space, message, Popconfirm, Card, Input as AntInput, Tag } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { excerptApi, type ExcerptItem, type CreateExcerptRequest } from '@/api/excerpt'

const { TextArea, Search } = AntInput
const { Option } = Select

/**
 * ============================================================================
 * 【每日摘录页面组件 (src/pages/excerpt/ExcerptPage.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 【React 核心概念：响应式分页与搜索组合 (Paginated State Management)】
 *    - 在大体量列表页面中，我们不能一次性加载所有数据，必须进行分页。
 *    - 我们声明分页状态：`const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 })`
 *    - 我们声明搜索状态：`const [searchKeyword, setSearchKeyword] = useState('')`
 *    - 我们使用副作用依赖这组筛选参数：`}, [pagination.current, pagination.pageSize, searchKeyword])`
 *    - **联动规律**：
 *      - 当用户点击底部的分页“第二页”，`pagination.current` 改变，触发 `fetchExcerpts` 加载第二页。
 *      - 当用户在输入框打字按下回车，触发 `setSearchKeyword`，副作用重新执行，自动以第一页拉取匹配的数据。
 *
 * 2. 多重嵌套列表与可选链渲染 (`tags?.map`)：
 *    - 在表格列的 `render` 中，如果需要遍历数组（比如展示摘录的标签列表 `tags`），
 *      建议使用 `tags?.map(tag => ...)`。
 *    - `tags` 可能由于后端数据结构未完善而呈现 `null` 或 `undefined`。可选链可以保全程序不发生致命的 `.map is not a function` 错误崩溃。
 *
 * 3. 动态按钮交互 (Dynamic State-Driven Icon)：
 *    - 在列表的“收藏”列中，我们使用 `record.isFavorite` 属性动态显示图标。
 *    - `icon={isFavorite ? <StarFilled style={{ color: '#faad14' }} /> : <StarOutlined />}`。
 *      通过布尔值控制图标是高亮黄色实心五星，还是普通空心五星，直接将数据状态转译为精美 UI。
 */
const ExcerptPage: React.FC = () => {
  // ============================================================================
  // // 状态（State）
  // ============================================================================
  const [loading, setLoading] = useState(false)
  const [excerpts, setExcerpts] = useState<ExcerptItem[]>([])
  const [modalVisible, setModalVisible] = useState(false)
  const [editingExcerpt, setEditingExcerpt] = useState<ExcerptItem | null>(null)
  const [form] = Form.useForm()
  
  // 搜索关键字状态
  const [searchKeyword, setSearchKeyword] = useState('')
  // 分页数据状态
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 })

  // ============================================================================
  // // 生命周期 (useEffect)
  // ============================================================================
  // 每当页码、页大小或搜索关键词改变时，自动重新请求列表数据
  useEffect(() => {
    fetchExcerpts()
  }, [pagination.current, pagination.pageSize, searchKeyword])

  // ============================================================================
  // // 数据加载方法
  // ============================================================================
  const fetchExcerpts = async () => {
    try {
      setLoading(true)
      const data = await excerptApi.list({
        page: pagination.current,
        pageSize: pagination.pageSize,
        keyword: searchKeyword
      })
      setExcerpts(data.list)
      // 更新分页状态中的总条数 (total)，以计算生成底部的页码数字按钮
      setPagination({ ...pagination, total: data.total })
    } catch (error) {
      console.error('获取摘录失败:', error)
    } finally {
      setLoading(false)
    }
  }

  // ============================================================================
  // // 交互行为
  // ============================================================================
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

  /**
   * 切换收藏状态
   */
  const handleToggleFavorite = async (record: ExcerptItem) => {
    try {
      // 1. 发起网络请求切换数据库中该记录的收藏状态
      await excerptApi.toggleFavorite(record.id)
      // 2. 给予用户轻提示
      message.success(record.isFavorite ? '已取消收藏' : '已收藏')
      // 3. 重新获取数据以同步最新收藏状态至列表 UI
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

  // ============================================================================
  // // 表格列定义
  // ============================================================================
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
          {/*
            安全解析列表：若 tags 有内容则遍历渲染，使用 tag.id 作为唯一 key，
            tag.color 提供 Antd Tag 的背景色。
          */}
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
        // 基于 isFavorite 条件渲染收藏图标
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
      {/* 顶部搜索排布通栏 */}
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>每日摘录</h2>
        <Space>
          {/* 关键字搜索框 */}
          <Search
            placeholder="搜索摘录内容"
            onSearch={(val) => {
              // 搜索时把页码重置为第一页，避免在第十页搜索导致无数据报错
              setSearchKeyword(val)
              setPagination(prev => ({ ...prev, current: 1 }))
            }}
            style={{ width: 300 }}
            allowClear
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建摘录
          </Button>
        </Space>
      </div>

      {/* 分页列表 */}
      <Table
        columns={columns}
        dataSource={excerpts}
        rowKey="id"
        loading={loading}
        pagination={{
          ...pagination,
          // 当用户点击底部分页器修改页码时，更新分页状态触发副作用 fetchExcerpts()
          onChange: (page, pageSize) => setPagination(prev => ({ ...prev, current: page, pageSize: pageSize || 10 }))
        }}
      />

      {/* 新建/编辑摘录弹窗 */}
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
